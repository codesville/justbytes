'''
Created on Mar 12, 2012

@author: nbkgl14
'''
from google.appengine.ext import db
import urllib, urllib2
from yaml import load
import logging
from urllib2 import URLError

class C2DM(object):
    
    def __init__(self):
        fh = open('conf.yaml')
        try:
            logging.info('Loading c2dm configs...');
            confYaml = load(fh)
            self.url = confYaml['c2dm.send.url']
            self.clientAuth = None
            self.registrationId = None
            self.collapseKey = None
            self.data = {}
        except Exception, e:
            logging.error('Failed to parse conf.yaml:' + str(e.message))
        finally:
            fh.close();
 
    def sendMessage(self):
        logging.info('Sending push notification to C2DM server')
        if self.registrationId == None or self.collapseKey == None:
            return False
        logging.info(str(self.data))
        values = {'registration_id' : self.registrationId,
                  'collapse_key' : self.collapseKey
                 }
        for k, v in self.data.iteritems():
            values['data.' + k] = v
        
        #values.update(self.data)
        headers = {'Authorization': 'GoogleLogin auth=' + self.clientAuth }
        data = urllib.urlencode(values)
        logging.info('Sending push notification to C2DM server %s with values %s and headers %s ' % (self.url, str(data), str(headers)))
        request = urllib2.Request(self.url, data, headers)
        
        try:
            response = urllib2.urlopen(request)
            responseString = response.read()
            logging.info('Received response for push notification:' + responseString)
            
        except URLError, e:
            logging.error("URLError: " + str(e))
            responseCode = e.code
            logging.error(responseCode)
        
        return responseString
        
    def getUsers(self):
        query = db.Query(UserInfo)
        return query.fetch(1000)
         

class UserInfo(db.Model):
    registration_id = db.StringProperty()
    device_id = db.StringProperty()
    email_id = db.StringProperty()
    

class C2DMClientAuth(object):
    _token = None
    

    def __init__(self):
        fh = open('conf.yaml')
        try:
            confYaml = load(fh)
            self.url = confYaml['c2dm.login.url']
            self.accountType = "GOOGLE"
            self.email = confYaml['c2dm.email']
            self.password = confYaml['c2dm.pass']
            self.source = confYaml['c2dm.source']
            self.service = 'ac2dm'
        except Exception, e:
            logging.error('Failed to parse conf.yaml:' + str(e.message))
        finally:
            fh.close();

    def getToken(self):
        if self._token is None:
            
            values = {'accountType' : self.accountType,
                      'Email' : self.email,
                      'Passwd' : self.password,
                      'source' : self.source,
                      'service' : self.service
                     }
            #logging.info('Get client login token request: ' + str(values))
            data = urllib.urlencode(values)
            request = urllib2.Request(self.url, data)
            
            response = urllib2.urlopen(request)
            responseAsString = response.read()
            
            responseList = responseAsString.split('\n')
            #logging.info('Received token response: ' + responseAsString)
            logging.info('Received token list: ' + str(responseList))
            for record in responseList:
                if record.split('=')[0] == 'Auth':
                    self._token = record.split('=')[1]
            logging.info('Token: ' + self._token)
        #    sys.stderr.flush()
        return self._token
    
