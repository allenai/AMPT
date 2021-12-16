---
title: CSV Column Configuration
summary: A brief overview of the pieces parts
authors:
    - Jenna James
    - Jordan Buckley
    - Paul Albee
date: 2021-04-22
---
## Column Configuration
This is an example of the `CVS-Columns.csv` configuration file. The file has a mandatory header row.


## Headers

| Header Column      | Description                                                                      |
|--------------------|----------------------------------------------------------------------------------|
| `column_name`      | The name of the column, must be unique. May contain embedded spaces.             |
| `description`      | A human-readable description of the column's content. Not currently used by AMPT.|
| `units`            | The measurement units. See [units](#units) table below.                          |
| `measurement_type` | The type of measurement. See [measurement type](#measurement-type) table below.  |
| `export`           | True if the column is exported for analysis outside of AMPT.                     |
| `editable`         | True if the column can be edited directly in the tool.                           | 
| `is_metadata`      | True if the column is image metadata.                                            |


## Units
| Unit               | Data Type                        |
|--------------------|----------------------------------|
| boolean            | Boolean flag, `True` or `False`. |
| editable text      | Free text.                       |
| fractional degrees | Stored as text.                  |
| fractional pixels  | Double precision floating point. |
| meters             | Double precision floating point. |
| millimeters        | Double precision floating point. |
| pixels             | Double precision floating point. |
| text               | Free text.                       |
| timestamp          | Free text.                       |


## Measurement Type
| Type        | Description                                                                       |
|-------------|-----------------------------------------------------------------------------------|
| auto length | Derived from some other measurement or copied from another length or auto length. |
| auto point  | Derived from another measurement, or copied from another point or auto point.     |
| boolean     | Binary flag value.                                                                |
| free text   | Any sort of textual input.                                                        |
| length      | Length measured by the user.                                                      |
| manual      | Entered manually.                                                                 |
| point       | Point set by user.                                                                |
| selection   | May be free text, or drawn from collection of existing values.                    |


## Required Rows
In order for AMPT to work correctly, there are a few mandatory rows in the configuration file. The mandatory rows are data sources and sinks fo different parts of the tool. The mandatory rows along with their usage are listed below, the names are case sensitive:

| Required Row     | Usage                                                                                                                         |
|------------------|-------------------------------------------------------------------------------------------------------------------------------|
|Filename          | This is the key for the measurements, must be unique for session. Linked to the "[Filename](metadata.md)" field.                                              |
|WhaleID           | This is the identifier for the animal being measured. Linked to the "[Animal ID](metadata.md)" field.                                        |
|Position          | This indicates where the animal is in the image. Linked to the "[Position](metadata.md)" field.                                              |
|UNDERWATER        | This indicates the depth of the animal 0 = On surface, 1 = Underwater, 2 =  Deep underwater. Linked to the "[Underwater](metadata.md)" field.|
|MEAS&nbsp;COMMENTS| This field holds free text comments from the user. Linked to the "[Comments](input.md#comments)" input panel.                                      |


## Default `CSV-Columns.csv`
```
column_name,description,units,measurement_type,export,editable,is_metadata
Filename,Name of the file used for the measurement,text,manual,True,False,True
TimePhoto,Time stamp of the photo,timestamp,manual,True,False,True
WhaleID,The <POD><NUMBER> designator for the animal,editable text,selection,True,True,True
Position,The <NUMBER>LR or <NUMBER>TB index for the animal being measured,editable text,manual,True,True,True
Type,"Type of animal, SR => Southern Resident Killer Whale",editable text,manual,True,True,True
Alt,Altitude from GPS,meters,manual,True,False,True
Laser,Altitude from laser altimeter,meters,manual,True,False,True
Lat,"Latitude, decimal notation",fractional degrees,manual,True,False,True
Lon,"Longitude, decimal notation",fractional degrees,manual,True,False,True
CameraMake,Brand of camera,text,manual,True,False,True
CameraModel,Model of camera,text,manual,True,False,True
FocalLength,Camera focal length,millimeters,manual,True,False,True
Comment,EXIF Field,text,manual,True,False,True
MEAS COMMENTS,Free text from scientist,editable text,free text,True,True,True
UNDERWATER,"Animal underwater, 0=No, 1=Yes",editable text,manual,True,True,True
GIRTH,breadth at anterior insertion of the dorsal fin (same as 100% measure in profile),fractional pixels,auto length,True,True,False
OFFSET,breadth of the animal (taken at 23% of the length of the blowhole to dorsal fin (BHDF)),fractional pixels,length,True,True,False
SNDF,tip of rostrum to anterior insertion of dorsal fin,fractional pixels,auto length,True,False,False
BHDF,center of blowhole to anterior insertion of dorsal fin,fractional pixels,auto length,True,False,False
DFFL,anterior insertion of dorsal fin to fluke notch,fractional pixels,auto length,True,False,False
LENGTH,tip of rostrum to fluke notch,fractional pixels,auto length,True,False,False
FW,Maximum breadth of fluke,fractional pixels,length,True,True,False
10%,Width of animal at 10% of distance of SNDF measurement,fractional pixels,length,True,True,False
20%,Width of animal at 20% of distance of SNDF measurement,fractional pixels,length,True,True,False
30%,Width of animal at 30% of distance of SNDF measurement,fractional pixels,length,True,True,False
40%,Width of animal at 40% of distance of SNDF measurement,fractional pixels,length,True,True,False
50%,Width of animal at 50% of distance of SNDF measurement,fractional pixels,length,True,True,False
60%,Width of animal at 60% of distance of SNDF measurement,fractional pixels,length,True,True,False
70%,Width of animal at 70% of distance of SNDF measurement,fractional pixels,length,True,True,False
80%,Width of animal at 80% of distance of SNDF measurement,fractional pixels,length,True,True,False
90%,Width of animal at 90% of distance of SNDF measurement,fractional pixels,length,True,True,False
100%,Width of animal at 100% of distance of SNDF measurement,fractional pixels,length,True,False,False
110%,Width of animal at 110% of distance of SNDF measurement,fractional pixels,length,True,True,False
120%,Width of animal at 120% of distance of SNDF measurement,fractional pixels,length,True,True,False
130%,Width of animal at 130% of distance of SNDF measurement,fractional pixels,length,True,True,False
140%,Width of animal at 140% of distance of SNDF measurement,fractional pixels,length,True,True,False
150%,Width of animal at 150% of distance of SNDF measurement,fractional pixels,length,True,True,False
160%,Width of animal at 160% of distance of SNDF measurement,fractional pixels,length,True,True,False
170%,Width of animal at 170% of distance of SNDF measurement,fractional pixels,length,True,True,False
180%,Width of animal at 180% of distance of SNDF measurement,fractional pixels,length,True,True,False
190%,Width of animal at 190% of distance of SNDF measurement,fractional pixels,length,True,True,False
200%,Width of animal at 200% of distance of SNDF measurement,fractional pixels,length,True,True,False
210%,Width of animal at 210% of distance of SNDF measurement,fractional pixels,length,True,True,False
220%,Width of animal at 220% of distance of SNDF measurement,fractional pixels,length,True,True,False
BH_x,Blowhole x position in image coordinates,pixels,point,False,False,False
BH_y,Blowhole y position in image coordinates,pixels,point,False,False,False
SN_x,Rostrum x position in image coordinates,pixels,point,False,False,False
SN_y,Rostrum y position in image coordinates,pixels,point,False,False,False
DF_x,Anterior insertion point of dorsal fin x position in image coordinates,pixels,point,False,False,False
DF_y,Anterior insertion point of dorsal fin y position in image coordinates,pixels,point,False,False,False
FL_x,Fluke notch x position in image coordinates,pixels,point,False,False,False
FL_y,Fluke notch y position in image coordinates,pixels,point,False,False,False
GIRTH_x_start,Starting X for breadth at anterior insertion of the dorsal fin (same as 100% measure in profile),pixels,auto point,False,False,False
GIRTH_y_start,Starting y for breadth at anterior insertion of the dorsal fin (same as 100% measure in profile),pixels,auto point,False,False,False
GIRTH_x_end,Ending X for breadth at anterior insertion of the dorsal fin (same as 100% measure in profile),pixels,auto point,False,False,False
GIRTH_y_end,Ending y for breadth at anterior insertion of the dorsal fin (same as 100% measure in profile),pixels,auto point,False,False,False
SNDF_x_start,Starting X for tip of rostrum to anterior insertion of dorsal fin,pixels,auto point,False,False,False
SNDF_y_start,Starting y for tip of rostrum to anterior insertion of dorsal fin,pixels,auto point,False,False,False
SNDF_x_end,Ending X for tip of rostrum to anterior insertion of dorsal fin,pixels,auto point,False,False,False
SNDF_y_end,Ending y for tip of rostrum to anterior insertion of dorsal fin,pixels,auto point,False,False,False
BHDF_x_start,Starting X for center of blowhole to anterior insertion of dorsal fin,pixels,auto point,False,False,False
BHDF_y_start,Starting y for center of blowhole to anterior insertion of dorsal fin,pixels,auto point,False,False,False
BHDF_x_end,Ending X for center of blowhole to anterior insertion of dorsal fin,pixels,auto point,False,False,False
BHDF_y_end,Ending y for center of blowhole to anterior insertion of dorsal fin,pixels,auto point,False,False,False
DFFL_x_start,Starting X for anterior insertion of dorsal fin to fluke notch,pixels,auto point,False,False,False
DFFL_y_start,Starting y for anterior insertion of dorsal fin to fluke notch,pixels,auto point,False,False,False
DFFL_x_end,Ending X for anterior insertion of dorsal fin to fluke notch,pixels,auto point,False,False,False
DFFL_y_end,Ending y for anterior insertion of dorsal fin to fluke notch,pixels,auto point,False,False,False
LENGTH_x_start,Starting X for tip of rostrum to fluke notch,pixels,auto point,False,False,False
LENGTH_y_start,Starting y for tip of rostrum to fluke notch,pixels,auto point,False,False,False
LENGTH_x_end,Ending X for tip of rostrum to fluke notch,pixels,auto point,False,False,False
LENGTH_y_end,Ending y for tip of rostrum to fluke notch,pixels,auto point,False,False,False
FW_x_start,Starting X for Maximum breadth of fluke,pixels,auto point,False,False,False
FW_y_start,Starting y for Maximum breadth of fluke,pixels,auto point,False,False,False
FW_x_end,Ending X for Maximum breadth of fluke,pixels,auto point,False,False,False
FW_y_end,Ending y for Maximum breadth of fluke,pixels,auto point,False,False,False
10%_x_start,Starting X for Width of animal at 10% of distance of SNDF measurement,pixels,auto point,False,False,False
10%_y_start,Starting y for Width of animal at 10% of distance of SNDF measurement,pixels,auto point,False,False,False
10%_x_end,Ending X for Width of animal at 10% of distance of SNDF measurement,pixels,auto point,False,False,False
10%_y_end,Ending y for Width of animal at 10% of distance of SNDF measurement,pixels,auto point,False,False,False
20%_x_start,Starting X for Width of animal at 20% of distance of SNDF measurement,pixels,auto point,False,False,False
20%_y_start,Starting y for Width of animal at 20% of distance of SNDF measurement,pixels,auto point,False,False,False
20%_x_end,Ending X for Width of animal at 20% of distance of SNDF measurement,pixels,auto point,False,False,False
20%_y_end,Ending y for Width of animal at 20% of distance of SNDF measurement,pixels,auto point,False,False,False
30%_x_start,Starting X for Width of animal at 30% of distance of SNDF measurement,pixels,auto point,False,False,False
30%_y_start,Starting y for Width of animal at 30% of distance of SNDF measurement,pixels,auto point,False,False,False
30%_x_end,Ending X for Width of animal at 30% of distance of SNDF measurement,pixels,auto point,False,False,False
30%_y_end,Ending y for Width of animal at 30% of distance of SNDF measurement,pixels,auto point,False,False,False
40%_x_start,Starting X for Width of animal at 40% of distance of SNDF measurement,pixels,auto point,False,False,False
40%_y_start,Starting y for Width of animal at 40% of distance of SNDF measurement,pixels,auto point,False,False,False
40%_x_end,Ending X for Width of animal at 40% of distance of SNDF measurement,pixels,auto point,False,False,False
40%_y_end,Ending y for Width of animal at 40% of distance of SNDF measurement,pixels,auto point,False,False,False
50%_x_start,Starting X for Width of animal at 50% of distance of SNDF measurement,pixels,auto point,False,False,False
50%_y_start,Starting y for Width of animal at 50% of distance of SNDF measurement,pixels,auto point,False,False,False
50%_x_end,Ending X for Width of animal at 50% of distance of SNDF measurement,pixels,auto point,False,False,False
50%_y_end,Ending y for Width of animal at 50% of distance of SNDF measurement,pixels,auto point,False,False,False
60%_x_start,Starting X for Width of animal at 60% of distance of SNDF measurement,pixels,auto point,False,False,False
60%_y_start,Starting y for Width of animal at 60% of distance of SNDF measurement,pixels,auto point,False,False,False
60%_x_end,Ending X for Width of animal at 60% of distance of SNDF measurement,pixels,auto point,False,False,False
60%_y_end,Ending y for Width of animal at 60% of distance of SNDF measurement,pixels,auto point,False,False,False
70%_x_start,Starting X for Width of animal at 70% of distance of SNDF measurement,pixels,auto point,False,False,False
70%_y_start,Starting y for Width of animal at 70% of distance of SNDF measurement,pixels,auto point,False,False,False
70%_x_end,Ending X for Width of animal at 70% of distance of SNDF measurement,pixels,auto point,False,False,False
70%_y_end,Ending y for Width of animal at 70% of distance of SNDF measurement,pixels,auto point,False,False,False
80%_x_start,Starting X for Width of animal at 80% of distance of SNDF measurement,pixels,auto point,False,False,False
80%_y_start,Starting y for Width of animal at 80% of distance of SNDF measurement,pixels,auto point,False,False,False
80%_x_end,Ending X for Width of animal at 80% of distance of SNDF measurement,pixels,auto point,False,False,False
80%_y_end,Ending y for Width of animal at 80% of distance of SNDF measurement,pixels,auto point,False,False,False
90%_x_start,Starting X for Width of animal at 90% of distance of SNDF measurement,pixels,auto point,False,False,False
90%_y_start,Starting y for Width of animal at 90% of distance of SNDF measurement,pixels,auto point,False,False,False
90%_x_end,Ending X for Width of animal at 90% of distance of SNDF measurement,pixels,auto point,False,False,False
90%_y_end,Ending y for Width of animal at 90% of distance of SNDF measurement,pixels,auto point,False,False,False
100%_x_start,Starting X for Width of animal at 100% of distance of SNDF measurement,pixels,auto point,False,False,False
100%_y_start,Starting y for Width of animal at 100% of distance of SNDF measurement,pixels,auto point,False,False,False
100%_x_end,Ending X for Width of animal at 100% of distance of SNDF measurement,pixels,auto point,False,False,False
100%_y_end,Ending y for Width of animal at 100% of distance of SNDF measurement,pixels,auto point,False,False,False
110%_x_start,Starting X for Width of animal at 110% of distance of SNDF measurement,pixels,auto point,False,False,False
110%_y_start,Starting y for Width of animal at 110% of distance of SNDF measurement,pixels,auto point,False,False,False
110%_x_end,Ending X for Width of animal at 110% of distance of SNDF measurement,pixels,auto point,False,False,False
110%_y_end,Ending y for Width of animal at 110% of distance of SNDF measurement,pixels,auto point,False,False,False
120%_x_start,Starting X for Width of animal at 120% of distance of SNDF measurement,pixels,auto point,False,False,False
120%_y_start,Starting y for Width of animal at 120% of distance of SNDF measurement,pixels,auto point,False,False,False
120%_x_end,Ending X for Width of animal at 120% of distance of SNDF measurement,pixels,auto point,False,False,False
120%_y_end,Ending y for Width of animal at 120% of distance of SNDF measurement,pixels,auto point,False,False,False
130%_x_start,Starting X for Width of animal at 130% of distance of SNDF measurement,pixels,auto point,False,False,False
130%_y_start,Starting y for Width of animal at 130% of distance of SNDF measurement,pixels,auto point,False,False,False
130%_x_end,Ending X for Width of animal at 130% of distance of SNDF measurement,pixels,auto point,False,False,False
130%_y_end,Ending y for Width of animal at 130% of distance of SNDF measurement,pixels,auto point,False,False,False
140%_x_start,Starting X for Width of animal at 140% of distance of SNDF measurement,pixels,auto point,False,False,False
140%_y_start,Starting y for Width of animal at 140% of distance of SNDF measurement,pixels,auto point,False,False,False
140%_x_end,Ending X for Width of animal at 140% of distance of SNDF measurement,pixels,auto point,False,False,False
140%_y_end,Ending y for Width of animal at 140% of distance of SNDF measurement,pixels,auto point,False,False,False
150%_x_start,Starting X for Width of animal at 150% of distance of SNDF measurement,pixels,auto point,False,False,False
150%_y_start,Starting y for Width of animal at 150% of distance of SNDF measurement,pixels,auto point,False,False,False
150%_x_end,Ending X for Width of animal at 150% of distance of SNDF measurement,pixels,auto point,False,False,False
150%_y_end,Ending y for Width of animal at 150% of distance of SNDF measurement,pixels,auto point,False,False,False
160%_x_start,Starting X for Width of animal at 160% of distance of SNDF measurement,pixels,auto point,False,False,False
160%_y_start,Starting y for Width of animal at 160% of distance of SNDF measurement,pixels,auto point,False,False,False
160%_x_end,Ending X for Width of animal at 160% of distance of SNDF measurement,pixels,auto point,False,False,False
160%_y_end,Ending y for Width of animal at 160% of distance of SNDF measurement,pixels,auto point,False,False,False
170%_x_start,Starting X for Width of animal at 170% of distance of SNDF measurement,pixels,auto point,False,False,False
170%_y_start,Starting y for Width of animal at 170% of distance of SNDF measurement,pixels,auto point,False,False,False
170%_x_end,Ending X for Width of animal at 170% of distance of SNDF measurement,pixels,auto point,False,False,False
170%_y_end,Ending y for Width of animal at 170% of distance of SNDF measurement,pixels,auto point,False,False,False
180%_x_start,Starting X for Width of animal at 180% of distance of SNDF measurement,pixels,auto point,False,False,False
180%_y_start,Starting y for Width of animal at 180% of distance of SNDF measurement,pixels,auto point,False,False,False
180%_x_end,Ending X for Width of animal at 180% of distance of SNDF measurement,pixels,auto point,False,False,False
180%_y_end,Ending y for Width of animal at 180% of distance of SNDF measurement,pixels,auto point,False,False,False
190%_x_start,Starting X for Width of animal at 190% of distance of SNDF measurement,pixels,auto point,False,False,False
190%_y_start,Starting y for Width of animal at 190% of distance of SNDF measurement,pixels,auto point,False,False,False
190%_x_end,Ending X for Width of animal at 190% of distance of SNDF measurement,pixels,auto point,False,False,False
190%_y_end,Ending y for Width of animal at 190% of distance of SNDF measurement,pixels,auto point,False,False,False
200%_x_start,Starting X for Width of animal at 200% of distance of SNDF measurement,pixels,auto point,False,False,False
200%_y_start,Starting y for Width of animal at 200% of distance of SNDF measurement,pixels,auto point,False,False,False
200%_x_end,Ending X for Width of animal at 200% of distance of SNDF measurement,pixels,auto point,False,False,False
200%_y_end,Ending y for Width of animal at 200% of distance of SNDF measurement,pixels,auto point,False,False,False
210%_x_start,Starting X for Width of animal at 210% of distance of SNDF measurement,pixels,auto point,False,False,False
210%_y_start,Starting y for Width of animal at 210% of distance of SNDF measurement,pixels,auto point,False,False,False
210%_x_end,Ending X for Width of animal at 210% of distance of SNDF measurement,pixels,auto point,False,False,False
210%_y_end,Ending y for Width of animal at 210% of distance of SNDF measurement,pixels,auto point,False,False,False
220%_x_start,Starting X for Width of animal at 220% of distance of SNDF measurement,pixels,auto point,False,False,False
220%_y_start,Starting y for Width of animal at 220% of distance of SNDF measurement,pixels,auto point,False,False,False
220%_x_end,Ending X for Width of animal at 220% of distance of SNDF measurement,pixels,auto point,False,False,False
220%_y_end,Ending y for Width of animal at 220% of distance of SNDF measurement,pixels,auto point,False,False,False
OFFSET_x_start,Starting X for breadth of the animal (taken at 23% of the length of the blowhole to dorsal fin (BHDF)),pixels,auto point,False,False,False
OFFSET_y_start,Starting y for breadth of the animal (taken at 23% of the length of the blowhole to dorsal fin (BHDF)),pixels,auto point,False,False,False
OFFSET_x_end,Ending X for breadth of the animal (taken at 23% of the length of the blowhole to dorsal fin (BHDF)),pixels,auto point,False,False,False
OFFSET_y_end,Ending y for breadth of the animal (taken at 23% of the length of the blowhole to dorsal fin (BHDF)),pixels,auto point,False,False,False
OFFSET_reviewed,Review of OFFSET length,boolean,boolean,False,False,False
FW_reviewed,Review of FW length,boolean,boolean,False,False,False
10%_reviewed,Review of 10% length,boolean,boolean,False,False,False
20%_reviewed,Review of 20% length,boolean,boolean,False,False,False
30%_reviewed,Review of 30% length,boolean,boolean,False,False,False
40%_reviewed,Review of 40% length,boolean,boolean,False,False,False
50%_reviewed,Review of 50% length,boolean,boolean,False,False,False
60%_reviewed,Review of 60% length,boolean,boolean,False,False,False
70%_reviewed,Review of 70% length,boolean,boolean,False,False,False
80%_reviewed,Review of 80% length,boolean,boolean,False,False,False
90%_reviewed,Review of 90% length,boolean,boolean,False,False,False
100%_reviewed,Review of 100% length,boolean,boolean,False,False,False
110%_reviewed,Review of 110% length,boolean,boolean,False,False,False
120%_reviewed,Review of 120% length,boolean,boolean,False,False,False
130%_reviewed,Review of 130% length,boolean,boolean,False,False,False
140%_reviewed,Review of 140% length,boolean,boolean,False,False,False
150%_reviewed,Review of 150% length,boolean,boolean,False,False,False
160%_reviewed,Review of 160% length,boolean,boolean,False,False,False
170%_reviewed,Review of 170% length,boolean,boolean,False,False,False
180%_reviewed,Review of 180% length,boolean,boolean,False,False,False
190%_reviewed,Review of 190% length,boolean,boolean,False,False,False
200%_reviewed,Review of 200% length,boolean,boolean,False,False,False
210%_reviewed,Review of 210% length,boolean,boolean,False,False,False
220%_reviewed,Review of 220% length,boolean,boolean,False,False,False
BH_reviewed,Review of BH point,boolean,boolean,False,False,False
SN_reviewed,Review of SN point,boolean,boolean,False,False,False
DF_reviewed,Review of DF point,boolean,boolean,False,False,False
FL_reviewed,Review of FL point,boolean,boolean,False,False,False
```