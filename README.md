# seene-toolkit
backup, modify, upload Seenes

# requirements
you need Java installed on your machine

should work on linux, windows and mac.

# usage

## using the GUI

warning: GUI is very rudimentary and still in development!!!

if you want to use GUI just type
`java -jar seene-tk.jar`

### First launch of the GUI

After the first launch you have to choose a dirctory where to store the seenes on your computer.

In that dialog you also can create a new directory for storing your seenes.

In the next dialog you should enter your seene credentials (same as Seene App).

You can change your settings later by choosing "Seene-Club -> Settings" in the top menu.

### First steps

Now you are ready to retrieve some of your seenes. Just choose "Tasks -> retrieve my public seenes" in the top menu.

After that step you should see the folders of the downloaded seenes on the left. 

Double Click on one of these folders to load the seene in the editor.

Now you can see the model [ show model ] or the poster [ show poster ] of that seene.

### Editing the seene model

if you place the mouse over the model / poster you can see a pink cursor for painting a mask over the model.

Use the mouse wheel to change the size of that cursor.

LEFT MOUSE BUTTON to draw the mask

RIGHT MOUSE BUTTON to erase the mask

MIDDLE MOUSE BUTTON or WHEEL CLICK shows you the depth information of point


To change the model underneath the mask use "Mask" Menu.

to be continued ...


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


# WARRANTY
## NO WARRANTY 





