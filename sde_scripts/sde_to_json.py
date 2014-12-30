#!/usr/local/bin/python

#
# EVE Online SDE data conversion script.
#
# (c) 2015 - Cameron Hotchkies <cameron@srs.bizn.as>
#
# This is useful for unifying all of the data available in the EVE 
# Static Data Export into one format.
# They seem to be moving towards YAML, but I find JSON to be easier to work
# with in Kartel, so that's what I'm using for now.
#

import json
import os
import codecs
import yaml

def get_sde_path():
    path_exists = False
    
    while not path_exists:
        print "Enter the path to your base SDE files. For more information"
        print "see the README on where to download this from"
        sde_path = raw_input("SDE path [sde]: ")
        
        if len(sde_path) == 0:
            sde_path = "sde"
            
        if os.path.exists(sde_path):
            path_exists = True
        else:
            print "\n[!] Directory %s was not found, try again\n" % (sde_path)
    
    return sde_path

def convert_sde_yaml(sde_path):
    print "[+] Converting YAML files from SDE"
    
    for filename in os.listdir(sde_path):
        if filename[-5:] == '.yaml':
            print "Processing %s" % (filename)
            infile = open(os.path.join(sde_path, filename))
            content = infile.read()
            data = yaml.load(content)
            
            # The yaml outputs a dict for the dataset, when everything else is
            # happy as a list
            intermediate = []
            for key in data:
                real_data = data[key]
                real_data['id'] = key
                intermediate.append(real_data)
                
            outdata = json.dumps(intermediate)
            
            outfile = open(os.path.join('json',filename.split('.')[0] + ".json"), "w")
            outfile.write(outdata)
            outfile.close()
            infile.close()

def repair_psv_newlines(header_count, content):
    magic_eol = "|##$EOL$##"
    
    # Re-join the content, since the existing newlines are not correct
    rejoined = "".join(content)
    
    resplit = rejoined.split(magic_eol)[:-1]
    
    return resplit

def convert_psv_files():
    print "[+] Converting PSV files from SDE SQL Server export"
    
    path_exists = False
    
    while not path_exists:
        print "Enter the path to your psv files. For more information"
        print "see the README on how to generate these"
        psv_path = raw_input("PSV path [psv]: ")
        
        if len(psv_path) == 0:
            psv_path = "psv"
            
        if os.path.exists(psv_path):
            path_exists = True
        else:
            print "\n[!] Directory %s was not found, try again\n" % (psv_path)
            
    for filename in os.listdir(psv_path):
        print "Processing: " + filename
        
        # Since this is coming from a windows dump, the encoding is likely latin1
        # which messes with the JSON output later down the road
        f = codecs.open(os.path.join(psv_path, filename), 'r', encoding='latin1')
        
        content = f.readlines()
        # Trim the last header as it's an EOL artifact
        headers = content[0].rstrip().split("|")[:-1] 
        # Trim the header underlines, as they have no value
        # then map it all to a list
        content = content[2:]
        
        repaired = repair_psv_newlines(len(headers), content)
        
        all_content = map(lambda c: c.strip().split("|"), repaired)
        
        results = []
        for c in all_content:
            entry = {}
            elements = zip(headers, c)
            for e in elements:
                val = e[1].replace('\r\n', '\n')
                entry[e[0]] = val if val != 'NULL' else None
                
            print entry
            results.append(entry)
        
        outfile = open(os.path.join('json',filename.split('.')[0] + ".json"), "w")
        print results
        output = json.dumps(results)
        
        outfile.write(output)
        outfile.close()
        f.close()
                
            
sde_path = get_sde_path()

convert_sde_yaml(sde_path)

convert_psv_files()