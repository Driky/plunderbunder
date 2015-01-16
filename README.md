Kartel
======

This is a web-app to interact with the EVE Online CREST API.

Functionality
=============

Build or Buy Manufacturing
--------------------------

Take a blueprint and determine if the submaterials (eg. T2 components) are better off built
or purchased. Prices are displayed from Jita sell orders and Jita split (lowest sell + highest buy) / 2

**Planned Features**

  * Supply ME factor for blueprints (who builds off of unresearched BPOs?)
  * Add alternate regions for pricing
  
Roadmap
=======

  * What uses what?
    * What use do certain subcomponents have (eg. High Tech Datachip, Carbon)
  * Does it reprocess?
    * Find all the crap in hangars and create a list of everything that has a 
      reprocess value within *x*% of sell price
    * Will account for Scrapmetal processing skill
  * Which items are trading below the reprocessed value

Transforming the EVE SDE
========================

The [EVE Static Data Export](https://developers.eveonline.com/resource/static-data-export) has
data available is a myriad of formats. The less portable being the MS SQL Server data back up.

To get the data into a format usable by Kartel, load up an AWS instance with MS SQL Server 2012
(don't use 2008, it won't work). Restore the database from inside of SQL Server Management Studio.

To dump the data into an easily processed format, run the following query in Management Studio:

    SELECT 
    'SQLCMD.EXE -S . -d ebs_DATADUMP -E ' +
    '-Q "SET NOCOUNT ON; SELECT *, ''##$EOL$##'' from ' + name + '" ' + 
    ' -o ' + name + '.psv -s"|" -w 2000 -W'
     FROM ebs_DATADUMP.sys.tables

Take the result of this query and paste all the lines into a .cmd file and run that script.

This will output the files in a pipe delimited format (safer than csv) which can then be downloaded
from the windows machine to your friendly *nix based platform.

From here you can run the `sde_to_json.py` script to convert all of the disparate data sources into 
one unified format. [This has been tested on both Rhea and Proteus SDE data sets]
