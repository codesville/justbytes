'''
Created on Mar 5, 2012

@author: nbkgl14
'''
import sys
import simplejson
from yaml import load
from framework import bottle
from framework.bottle import route, template, request, error, debug
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import blobstore
 
@route('/')
def DisplayForm():
    message = 'Just Bytes: Under Construction!!'
    output = template('templates/home', data=message)
    return output

@route('/ITechQuiz/getLatestQandA', method='GET')
def GetLatestQandA():
    userVer = int(request.query.ver)
    latestVer = int(GetMaxVer())
    
    jsonFile = {}
    # creating output dictionary of list/array
    outputDict = {}
    outputDict['QandAList'] = []
    
    if userVer < latestVer:
        #Merge all update files into one JSON output in case user is way behind in updates
        for i in range(userVer + 1, latestVer + 1):
            try:
                sys.stderr.write('Loading file:' + 'upload/updates_' + str(i) + '.json')
                fh = open('upload/updates_' + str(i) + '.json', 'r')
                # load json file
                jsonFile = simplejson.load(fh)
                # extend list to add jsonFile rows
                outputDict['QandAList'].extend(jsonFile['QandAList']) 
                
            except Exception, e:
                sys.stderr.write('Exception reading file:' + str(e.message))
            finally:
                fh.close()
    return outputDict

# Reads conf.yaml to find the lastest version of questions    
def GetMaxVer():
    fh = open('upload/conf.yaml')
    try:
        confYaml = load(fh)
        return confYaml['maxversion']
    except Exception, e:
        sys.stderr.write('Failed to parse maxver.yaml:' + str(e.message))
    finally:
        fh.close();
    
     
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
