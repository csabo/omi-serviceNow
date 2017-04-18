#!/usr/bin/env ruby

#the entire event record is passed to the script https://sensuapp.org/docs/0.27/reference/events.html#event-data
require "uri"
require "net/http"
require "json"
require "openssl"

#file = open("event.json")
#json = file.read
#eventRecord = JSON.parse(json)
string = $stdin.gets.chomp
eventRecord = JSON.parse(string)
eventId = eventRecord["id"]
eventOccurences = eventRecord["occurrences"]
eventTitle = eventRecord["check"]["output"]
eventName = eventRecord["check"]["name"]
eventSystemName = eventRecord["client"]["name"]
eventSystemAddress = eventRecord["client"]["address"]

url = URI("https://OMI_HOST/opr-web/rest/9.10/event_list")

http = Net::HTTP.new(url.host, url.port)
http.use_ssl = true
http.verify_mode = OpenSSL::SSL::VERIFY_NONE

request = Net::HTTP::Post.new(url)
request['authorization'] = 'Basic AUTH_GOES_HERE'
request['content-type'] = 'application/xml;charset=UTF-8'
request.body = "<event xmlns=\"http://www.hp.com/2009/software/opr/data_model\">\n<title>#{eventTitle}</title>\n<severity>normal</severity>\n<priority>low</priority>\n<state>open</state>\n<description>#{eventRecord}</description>\n<source_ci_hints type=\"urn:x-hp:2009:software:data_model:opr:type:source_ci_hints\" version=\"1.0\">\n<hint>OmCoreId:a587e47e-20c0-758d-0d58-c0778d208575</hint>\n<node type=\"urn:x-hp:2009:software:data_model:opr:type:node_info\" version=\"1.0\">\n<dns_name>#{eventSystemName}</dns_name>\n<ip_address>#{eventSystemAddress}</ip_address>\n</node>\n</source_ci_hints>\n</event>"

response = http.request(request)
