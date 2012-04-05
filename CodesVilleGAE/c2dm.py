'''
Created on Mar 12, 2012

@author: nbkgl14
'''
from google.appengine.ext import db
import urllib, urllib2
from yaml import load
import sys
from urllib2 import URLError

class C2DM(object):
    
    def __init__(self):
        fh = open('conf.yaml')
        try:
            confYaml = load(fh)
            self.url = confYaml['c2dm.send.url']
            self.clientAuth = None
            self.registrationId = None
            self.collapseKey = None
            self.data = {}
        except Exception, e:
            sys.stderr.write('Failed to parse conf.yaml:' + str(e.message))
        finally:
            fh.close();
 
    def sendMessage(self):
        if self.registrationId == None or self.collapseKey == None:
            return False
        
        for k, v in self.data.iteritems():
            self.data['data.' + k] = v
        
        values = {'registration_id' : self.registrationId,
                  'collapse_key' : self.collapseKey
                 }
        headers = {'headers': 'GoogleLogin auth=' + self.clientAuth }
        data = urllib.urlencode(values)
        request = urllib2.Request(self.url, data, headers)
        
        try:
            response = urllib2.urlopen(request)
            responseString = response.read()
            sys.stderr.write(responseString)
            
        except URLError, e:
            sys.stderr.write("URLError: " + str(e))
            responseCode = e.code
            sys.stderr.write(responseCode)
        
        return responseString
        
    def getUsers(self):
        query = db.Query(UserInfo)
        return query.fetch(1000000)
         

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
            sys.stderr.write('Failed to parse conf.yaml:' + str(e.message))
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
            data = urllib.urlencode(values)
            request = urllib2.Request(self.url, data)
            
            response = urllib2.urlopen(request)
            responseAsString = response.read()
            
            responseList = responseAsString.split('\n')
            sys.stderr.write(responseAsString)
            self._token = responseList[2].split['='][1]
        return self._token
    
