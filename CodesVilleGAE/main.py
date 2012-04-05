'''
Created on Mar 5, 2012

@author: nbkgl14
'''
from framework import bottle
from framework.bottle import route, template, request, error, debug
from google.appengine.api.datastore import Query
from google.appengine.ext import db
from google.appengine.ext.webapp.util import run_wsgi_app
from yaml import load
import c2dm
import datetime
import simplejson
import sys

 
@route('/')
def DisplayForm():
    message = 'Just Bytes: Under Construction!!'
    output = template('templates/home', data=message)
    return output

@route('/ITechQuiz/getLatestQandA', method='GET')
def GetLatestQandA():
    userVer = int(request.GET.get('ver', ''))
    latestVer = GetMaxVer()
    
    jsonFile = {}
    # creating output dictionary of list/array
    outputDict = {}
    outputDict['QandAList'] = []
    
    if userVer < latestVer:
        #Merge all update files into one JSON output in case user is way behind in updates
        for i in range(userVer + 1, latestVer + 1):
            fh = None
            try:
                sys.stderr.write('Loading file:' + 'upload/updates_' + str(i) + '.json ')
                fh = open('upload/updates_' + str(i) + '.json', 'r')
                if fh:
                    # load json file
                    jsonFile = simplejson.load(fh)
                    # extend list to add jsonFile rows
                    outputDict['QandAList'].extend(jsonFile['QandAList']) 
                
            except Exception, e:
                sys.stderr.write('Exception reading file:%s' % str(e))
            finally:
                if fh:
                    fh.close()
        # fetch latest user posted questions
        dsQandADict = GetLatestQandAFromDS(userVer)
        if dsQandADict:
            outputDict['QandAList'].extend(dsQandADict['QandAList'])
        
    return outputDict

@route('/ITechQuiz/postQandA', method='POST')        
def PostQandA():
    userName = request.POST.get('username', '').strip()
    question = request.POST.get('question', '').strip()
    answer = request.POST.get('answer', '').strip()
    topicId = int(request.POST.get('topicId', '').strip())
    postedTime = datetime.datetime.replace(tzinfo=datetime.tzinfo.EST).now()
    
    try:
        newVer = GetMaxVer() + 1
        
        # create datastore entity
        qanda = QandA(username=userName, topic_id=topicId, question=question, answer=answer, posted_time=postedTime, version=newVer)
        # save it in datastore
        db.put(qanda)
        
        # notify C2DM servers
        clientAuth = c2dm.C2DMClientAuth()
        token = clientAuth.getToken()
        sender = c2dm.C2DM()
        sender.clientAuth = token
        sender.collapseKey = 1
        sender.data = {'message':'New question has been posted.'}
        
        # send notification to each user
        for user in sender.getUsers():
            sender.registrationId = user.registration_id
            response = sender.sendMessage()
        
    except Exception, e:
        sys.stderr.write('Failed to save QandA: %s' % str(e))
    finally:
        pass

@route('/ITechQuiz/register', method='POST')
def Register():
    deviceId = request.POST.get('deviceId', '').strip()
    emailId = request.POST.get('emailId', '').strip()
    registrationId = request.POST.get('regId', '').strip()
    
    userInfo = c2dm.UserInfo(device_id=deviceId, email_id=emailId, registration_id=registrationId)
    # TODO: if regId changes, then need to update the regId and not insert new
    db.put(userInfo)

def GetLatestQandAFromDS(curVer):
    jsonList = []
    try:
        query = db.Query(QandA)
        query.filter('version > ', curVer)
        result = query.fetch(10000)
        
        for qanda in result:
            qandaRow = {}
            qandaRow['username'] = qanda.username
            qandaRow['topic_id'] = str(qanda.topic_id)
            qandaRow['question'] = qanda.question
            qandaRow['answer'] = qanda.answer
            qandaRow['posted_time'] = str(qanda.posted_time)
            qandaRow['version'] = qanda.version
            jsonList.append(qandaRow)
    except Exception, ex:
        sys.stderr.write('Failed to query datastore: %s' % str(ex))
    
    qandaDict = {}
    if jsonList:
        qandaDict['QandAList'] = jsonList
    #sys.stderr.write(qandaDict)
    return qandaDict

# Defaults to 1. Fetch max version of QandA from DS(for user uploads).
# Fetch max version from conf.yaml(for admin uploads).Compare the 2. Return the highest     
def GetMaxVer():
    # default to 1
    maxVer = 1
    dsVer = 1
    #Read maxVer from conf.yaml
    fh = open('conf.yaml')
    try:
        confYaml = load(fh)
        maxVer = int(confYaml['qanda.maxversion'])
    except Exception, e:
        sys.stderr.write('Failed to parse maxver.yaml:%s' % str(e))
    finally:
        fh.close();
    # Read max(version) from DS
    query = db.Query(QandA)
    query.order('-version') #desc
    row = query.fetch(1)
    
    if row:
        dsVer = row[0].version
    maxVer = dsVer if dsVer > maxVer else maxVer
    sys.stderr.write('Max version: %s' % maxVer)
    return maxVer
    


class QandA(db.Model):
    username = db.StringProperty()
    version = db.IntegerProperty(required=True)
    question = db.StringProperty(required=True)
    answer = db.StringProperty(required=True)
    topic_id = db.IntegerProperty(required=True)
    posted_time = db.DateTimeProperty()
    
            
     
def main():
    debug(True)
    run_wsgi_app(bottle.default_app())
    
    
#def uploadFile():
    #blobstore.create_upload_url('/upload/')
 
@error(403)
def Error403(code):
    return 'Error 403: Please check the URL'
 
@error(404)
def Error404(code):
    return 'Error 404: File not found. Please check the URL'
 
if __name__ == "__main__":
    main()
