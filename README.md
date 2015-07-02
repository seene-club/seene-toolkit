# seene-toolkit
backup, modify, upload Seenes

# requirements
you need Java installed on your machine
should work on linux, windows and mac.

# usage

## using COMMAND LINE

### BACKUPS

task: BACKUP your public Seenes

`java -jar seene-tk.jar -b public -uid <SeeneUsername>`


task: BACKUP your private Seenes

`java -jar seene-tk.jar -b private -uid <SeeneUsername>`


### UPLOADING

task: UPLOAD a Seene to your PRIVATE Seenes

put the scene.oemodel and the poster.jpg files next to the seene-tk.jar and type

`java -jar seene-tk.jar -u -uid <SeeneUsername>`


## using the GUI

warning: GUI is very rudimentary and still in development!!!

if you want to use GUI just type
`java -jar seene-tk.jar`




