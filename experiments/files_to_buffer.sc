// We want to read in a bunch of files to use, and they are in a different
// folder. To do this, we can use a dictionary to store the sounds in each
// directory so that we know what we are referencing.
(
var add_entries;
~here = if (Platform.ideName == "scqt",       // Test if we are in scide
    { thisProcess.nowExecutingPath.dirname }, // only works interactively
    { "/Users/zhian/Documents/Learning/supercollider/learning-supercollider/" }                           // assume we used sclang -p $(pwd)
);

// add entries to a dictionary of arrays containing buffers called "b"
//
// @param subfolder a folder that contains either soundfiles or other folders
// @param parent the name of a parent folder. Used as part of the key to the
//    dictionary
//
// @return a dictionary of buffer arrays based on folder structure
add_entries = {
    // Recursive function to add entries of nested folders to a dictionary
    // Each entry in the dictionary will contain sound files in that folder
    arg subfolder, parent = "";

    var key;
    key = (parent ++ "_" ++ subfolder.folderName).asSymbol;
    // step 1: add all the files in an array
    if (subfolder.files.size > 0) {
        b.add(
            key ->
            Array.fill(
                subfolder.files.size,
                {
                    arg i;
                    Buffer.read(s, subfolder.files[i].fullPath);
                }
            )
        );
    } {};
    // step 2: if there are any folders underneath, recurse;
    if (subfolder.folders.size > 0) {
        subfolder.folders.do{
            arg subsub;
            add_entries.(subsub, key);
        };
    } {};
};

// STEP 1: create a new dictionary
b = Dictionary.new;

// STEP 2: loop over all the entries in the "sounds" folder.
PathName(~here +/+ "../sounds").entries.do{
    arg subfolder;
    if (subfolder.isFolder) {
        add_entries.(subfolder, "sounds");
    } {
        // If there is a sound file, add it as a buffer
        b.add(
            ("sounds_" ++ subfolder.fileNameWithoutExtension).asSymbol ->
            Array.fill(
                1, Buffer.read(s, subfolder.fullPath)
            )
        )
    };
};
)

b.keys;
b[\sounds_bubbles][0].play;