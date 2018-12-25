s.boot;
// To use a sound file, we can use the Buffer.read() method
~b0 = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");
~huh = Buffer.read(s, Platform.userHomeDir +/+ "Desktop/audio\ projects/laughing/samples/huhuh.aiff")
~b0.play;
~huh.play;

// Buffers can be zeroed out, but left on the server
~huh.zero;
~huh.play;
~huh.read(Platform.userHomeDir +/+ "Desktop/audio\ projects/laughing/samples/huhuh.aiff")
~huh.play;



// Buffer.freeAll; // removes all buffers

// Frames: every buffer has a certain number of frames representing the length of
// the buffer.
~b0.numFrames;
~huh.numFrames;

// Channels
~b0.numChannels;
~huh.numChannels;
// you can use Buffer.readChannels() to read individual channels


// Samples
// numSamples = numFrames * numChannels

// Duration
~b0.duration;
~huh.duration;

// Buffer Numbers (bufnums)
~b0.bufnum;
~huh.bufnum;

// Buffer Sample Rate
// Good to check these so that the sounds aren't unintentionally resampled.
~b0.sampleRate;
~huh.sampleRate;
s.sampleRate;

// Summary
~b0.query;
~huh.query;

// Read part of a soundfile ---------------------------------------
// Let's say we wanted to read only a quarter of a second from huh
(
~huh25 = Buffer.read(s,
    Platform.userHomeDir +/+ "Desktop/audio\ projects/laughing/samples/huhuh.aiff",
    startFrame: 0,
    numFrames: s.sampleRate/4
);
)
~huh25.play;
~huh.play;

// To get the end of a soundfile, you will need to know how many frames are in the
// soundfile and then subtract from there
(
~b0_over = Buffer.read(s,
    Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff",
    startFrame: ~b0.numFrames - (s.sampleRate*0.90),
    numFrames: s.sampleRate*0.90
);
)
~b0_over.play;

// Read in several files in at once -------------------------------
// We can load a bunch of buffers into an array
~drms = Array.new;
~folder = PathName.new("/Users/zhian/Desktop/audio projects/Peanut/drums/");
(
~folder.entries.do({
    arg path;
    ~drms = ~drms.add(Buffer.read(s, path.fullPath));
});
)

~drms[0].play;
~drms[1].play;
~drms[2].play;
~drms[3].play;
~drms[4].play;
~drms[5].play;
~drms[6].play;

// Manipulating buffers

// PlayBuf and BufRead


(
SynthDef.new(\playbuf, {
    arg amp = 1, out = 0, buf, rate = 1, t_trig = 1, start = 0, loop = 0, da = 2;
    var sig;
    sig = PlayBuf.ar(
        numChannels: 2, // This must be a fixed number
        bufnum: buf,
        rate: BufRateScale.kr(buf) * rate, // protects against lower rate samples
        trigger: t_trig,
        startPos: start,
        loop: loop,
        doneAction: da
    );
    sig = sig * amp;
    Out.ar(out, sig);
}).add;
)

Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 1])
Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 2])

Synth.new(\playbuf, [\buf, ~drms[5].bufnum, \amp, 0.25, \rate, 3])

// Transposition by semitones can be accomplished with the midiratio method
12.midiratio
-12.midiratio
7.midiratio
Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 1])
Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 7.midiratio])


