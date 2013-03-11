'''
Created on Mar 12, 2012

@author: nbkgl14
'''
import urllib, urllib2
from yaml import load
import sys

#unused
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
