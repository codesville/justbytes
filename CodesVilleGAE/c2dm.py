'''
Created on Mar 12, 2012

@author: codesville
$Id$
'''
from google.appengine.api import urlfetch
from google.appengine.ext import db
from urllib2 import URLError
from yaml import load
import datetime
import json
import logging

class C2DM(object):
    
    def __init__(self):
        fh = open('conf.yaml')
        try:
            confYaml = load(fh)
            self.url = confYaml['gcm.send.url']
            # self.clientAuth = None
            self.registrationIds = []
            self.collapseKey = None
            self.data = {}
        except Exception, e:
            logging.error('Failed to parse conf.yaml:' + str(e.message))
        finally:
            fh.close()
 
 
    def sendMessage(self):
        logging.info('Sending push notification to GCM server')
        if self.registrationIds == None or len(self.registrationIds) == 0:
            logging.warn('Empty registrationIds. Nothing to publish')
            return False
        values = {'registration_ids' : self.registrationIds,
                  'collapse_key' : str(self.collapseKey),
                  'data' : self.data,
                 }
        
            
        # value of key( server apps) is copied from the APIs console
        serverApiKey = 'AIzaSyA2rsydBFVpHhbUOOSjK0yqO3506zvRY80'
        
        headers = {'Content-Type': 'application/json', 'Authorization': 'key=%s' % serverApiKey }
        data = json.dumps(values)
        # data = urllib.urlencode(data)
        
        try:
            response = urlfetch.Fetch(url=self.url, payload=data, method=urlfetch.POST, headers=headers)
            responseString = response.content
            self.handleErrors(responseString, self.registrationIds)
            logging.info('Received response for push notification %s with response code=%s' % (responseString, str(response.status_code)))
            
        except URLError, e:
            logging.error("URLError: " + str(e))
            responseCode = e.code
            logging.error(responseCode)
            
        return responseString
    
    def handleErrors(self, responseString, regIds):
        try:
            respJson = json.loads(responseString)
            mapping = zip(regIds, respJson['results'])
            errorMapping = filter(lambda x: "error" in x[1], mapping)
            for regId, errorDict in errorMapping:
                logging.info('Received error for regId %s with msg=%s' % (regId, errorDict['error']))
                if errorDict['error'] == 'NotRegistered':
                    self.unregisterUser(regId)
        except Exception, e:
            logging.error('Failed to handleErrors:' + str(e.message))
            
            
    def unregisterUser(self, regId):
        try:     
            query = db.GqlQuery('select * from UserInfo where registration_id= :1', regId)
            row = query.fetch(1)
            if row:
                row[0].is_active = False
                row[0].update_time = datetime.datetime.now()  # Needs fixing to EST
                logging.info('Unregistering device %s from server' % row[0].device_id)
                db.put(row[0])
        except Exception, e:
            logging.error('Failed to unregister user:' + str(e.message))
        
    def getUsers(self):
        query = db.Query(UserInfo)
        query.filter('is_active', True)
        return query.fetch(10000)
         

class UserInfo(db.Model):
    registration_id = db.StringProperty()
    device_id = db.StringProperty()
    email_id = db.StringProperty()
    gcm_c2dm = db.StringProperty(required=False)
    is_active = db.BooleanProperty(required=False)
    update_time = db.DateTimeProperty(required=False)
    
###### NOT USED ANY MORE AFTER MIGRATING TO GCM
# class C2DMClientAuth(object):
#     _token = None
#     
# 
#     def __init__(self):
#         fh = open('conf.yaml')
#         try:
#             confYaml = load(fh)
#             self.url = confYaml['c2dm.login.url']
#             self.accountType = "GOOGLE"
#             self.email = confYaml['c2dm.email']
#             self.password = confYaml['c2dm.pass']
#             self.source = confYaml['c2dm.source']
#             self.service = 'ac2dm'
#         except Exception, e:
#             logging.error('Failed to parse conf.yaml:' + str(e.message))
#         finally:
#             fh.close();
# 
#     def getToken(self):
#         if self._token is None:
#             
#             values = {'accountType' : self.accountType,
#                       'Email' : self.email,
#                       'Passwd' : self.password,
#                       'source' : self.source,
#                       'service' : self.service
#                      }
#             # logging.info('Get client login token request: ' + str(values))
#             data = urllib.urlencode(values)
#             request = urllib2.Request(self.url, data)
#             
#             response = urllib2.urlopen(request)
#             responseAsString = response.read()
#             
#             responseList = responseAsString.split('\n')
#             # logging.info('Received token response: ' + responseAsString)
#             logging.info('Received token list: ' + str(responseList))
#             for record in responseList:
#                 if record.split('=')[0] == 'Auth':
#                     self._token = record.split('=')[1]
#             logging.info('Token: ' + self._token)
#         #    sys.stderr.flush()
#         return self._token
    
