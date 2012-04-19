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
import logging

 
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
                logging.info('Loading file:' + 'upload/updates_' + str(i) + '.json ')
                fh = open('upload/updates_' + str(i) + '.json', 'r')
                if fh:
                    # load json file
                    jsonFile = simplejson.load(fh)
                    # extend list to add jsonFile rows
                    outputDict['QandAList'].extend(jsonFile['QandAList']) 
                
            except Exception, e:
                logging.error('Exception reading file:%s' % str(e))
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
    postedTime = datetime.datetime.now() #Needs fixing to EST
    
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
        
        query = db.GqlQuery('select * from Topics where topic_id = :1', topicId)
        row = query.fetch(1)
        msg = 'New ' + row[0].category
        if row[0].title != 'All':
            msg += '(' + row[0].title + ')' 
        msg += ' question has been posted.' 
        #logging.info('Sending new QandA push notification...' + msg)
        
        sender.data = {'message':msg}
        
        # send notification to each user
        for user in sender.getUsers():
            #logging.info('..to ' + user.registration_id)
            sender.registrationId = user.registration_id
            response = sender.sendMessage()
            #logging.info(response)
        
    except Exception, e:
        logging.error('Failed to save QandA: %s' % str(e))
    finally:
        pass
        #sys.stderr.flush()

@route('/ITechQuiz/register', method='POST')
def Register():
    deviceId = request.POST.get('deviceId', '').strip()
    #emailId = request.POST.get('emailId', '').strip()
    emailId = 'test'
    registrationId = request.POST.get('regId', '').strip()
    logging.info('Received request to register device owner %s' % registrationId)
    userInfo = c2dm.UserInfo(device_id=deviceId, email_id=emailId, registration_id=registrationId)
    # TODO: if regId changes, then need to update the regId and not insert new
    query = db.GqlQuery('select * from UserInfo where device_id= :1', deviceId)
    row = query.fetch(1)
    if row:
        logging.info('Found existing deviceId %s' % row[0])
        logging.info('Updating registrationdId of device %s to %s' % (deviceId, registrationId))
        row[0].registration_id = registrationId
        db.put(row[0])
    else:
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
        logging.error('Failed to query datastore: %s' % str(ex))
    
    qandaDict = {}
    if jsonList:
        qandaDict['QandAList'] = jsonList
    #logging.info(qandaDict)
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
        logging.error('Failed to parse maxver.yaml:%s' % str(e))
    finally:
        fh.close();
    # Read max(version) from DS
    query = db.Query(QandA)
    query.order('-version') #desc
    row = query.fetch(1)
    
    if row:
        dsVer = row[0].version
    maxVer = dsVer if dsVer > maxVer else maxVer
    logging.info('Max version: %s' % maxVer)
    return maxVer
    


class QandA(db.Model):
    username = db.StringProperty()
    version = db.IntegerProperty(required=True)
    question = db.StringProperty(required=True)
    answer = db.StringProperty(required=True)
    topic_id = db.IntegerProperty(required=True)
    posted_time = db.DateTimeProperty()
    

class Topics(db.Model):
    topic_id = db.IntegerProperty(required=True)
    version = db.IntegerProperty(required=True)
    title = db.StringProperty(required=True)
    category = db.StringProperty(required=True)
    
def loadTopics():
    topArray = []
    topArray.append(Topics(topic_id=1, version=1, title='Fundamentals', category='Java'))
    topArray.append(Topics(topic_id=2, version=1, title='Classes/Objects', category='Java'))
    topArray.append(Topics(topic_id=3, version=1, title='JVM/GC', category='Java'))
    topArray.append(Topics(topic_id=5, version=1, title='Collections', category='Java'))
    topArray.append(Topics(topic_id=6, version=1, title='Design Patterns', category='Java'))
    topArray.append(Topics(topic_id=7, version=1, title='Exceptions', category='Java'))
    topArray.append(Topics(topic_id=8, version=1, title='Persistence', category='Java'))
    topArray.append(Topics(topic_id=9, version=1, title='File I/O and Networking', category='Java'))
    topArray.append(Topics(topic_id=10, version=1, title='Threads', category='Java'))
    topArray.append(Topics(topic_id=11, version=1, title='Basics/Framework', category='.NET'))
    topArray.append(Topics(topic_id=12, version=1, title='OOPS/C#', category='.NET'))
    topArray.append(Topics(topic_id=13, version=1, title='ASP.NET', category='.NET'))
    topArray.append(Topics(topic_id=14, version=1, title='ADO.NET', category='.NET'))
    topArray.append(Topics(topic_id=15, version=1, title='LINQ', category='.NET'))
    topArray.append(Topics(topic_id=16, version=1, title='WCF', category='.NET'))
    topArray.append(Topics(topic_id=17, version=1, title='WPF/Silverlight', category='.NET'))
    topArray.append(Topics(topic_id=18, version=1, title='Design Patterns', category='.NET'))
    topArray.append(Topics(topic_id=19, version=1, title='All', category='Sql'))
    topArray.append(Topics(topic_id=20, version=1, title='All', category='Unix'))
    topArray.append(Topics(topic_id=21, version=1, title='All', category='Hibernate'))
    topArray.append(Topics(topic_id=22, version=1, title='All', category='Spring'))
    topArray.append(Topics(topic_id=23, version=1, title='Servlets', category='Java'))
    topArray.append(Topics(topic_id=24, version=1, title='Struts', category='Java'))
    topArray.append(Topics(topic_id=25, version=1, title='JMS', category='Java'))
    
    for top in topArray:
        db.put(top)
     
def main():
    debug(True)
    run_wsgi_app(bottle.default_app())
    loadTopics()
    
    
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
