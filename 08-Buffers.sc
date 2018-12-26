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
Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 12.midiratio])

// We can use groups to group these all together
g = Group.new;
(
x = Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 1.5, \loop, 1, \amp, 0.5], target: g);
y = Synth.new(\playbuf, [\buf, ~huh25.bufnum, \rate, 0.75, \loop, 1, \amp, 0.5], target: g);
z = Synth.new(\playbuf, [\buf, ~huh.bufnum, \rate, 2.5, \loop, 1, \amp, 0.5], target: g);
)

g.set(\rate, 0.5);
g.set(\buf, ~huh.bufnum);
x.set(\rate, exprand(0.2, 2.0));
y.set(\rate, exprand(0.2, 2.0));
z.set(\rate, exprand(0.2, 2.0));
g.set(\loop, 0)
g.free;


// BufRd

(
SynthDef.new(\bufrd, {
    arg amp = 1, out = 0, buf, start, end;
    var sig, ptr;
    ptr = Line.ar(
        start: start,
        end:   end,
        dur:   BufDur.kr(buf),
        doneAction: 2
    );
    sig = BufRd.ar(
        numChannels: 2,
        bufnum: buf,
        phase: ptr
    );
    sig = sig * amp;
    Out.ar(out, sig);
}).add;
)

// Play the file forward
Synth.new(\bufrd, [\buf, ~huh.bufnum, \start, 0, \end, ~huh.numFrames - 1]);
// Play the file backward
Synth.new(\bufrd, [\buf, ~huh.bufnum, \end, 0, \start, ~huh.numFrames - 1]);

// Looping with buffrd
// The Phasor UGen is good for looping. "A resettable linear amp between two levels"
// Two other related UGens are:
//  1. LFSaw
//  2. Sweep
(
SynthDef.new(\bufrd_loop, {
    arg amp = 1, out = 0, buf, start, end, rate = 1;
    var sig, ptr;
    ptr = Phasor.ar(
        trig: 0,
        rate: BufRateScale.kr(buf) * rate,
        start: start,
        end: end
    );
    sig = BufRd.ar(
        numChannels: 2,
        bufnum: buf,
        phase: ptr
    );
    sig = sig * amp;
    Out.ar(out, sig);
}).add;
)
x = Synth.new(\bufrd_loop, [\buf, ~huh.bufnum, \start, 0, \end, ~huh.numFrames - 1]);
x.set(\start, ~huh.numFrames/5, \end, 2*(~huh.numFrames/5), \rate, 1, \amp, 1);
x.set(\start, 4000, \end, 900, \rate, 0.05, \amp, 5);
x.set(\start, ~huh.numFrames/5, \end, 2*(~huh.numFrames/5));
x.free;

// We can use ANY audio rate (ar) UGen as a frame pointer. Here, we can use SinOsc
// To read a buffer forward and backward sinusoidally.
(
SynthDef.new(\bufrd_sin, {
    arg amp = 1, out = 0, buf, start, end, freq = 1;
    var sig, ptr;
    ptr = SinOsc.ar(
        freq: freq,
        phase: 3pi/2     // lowest point of sinewave (the start of the track)
    ).range(start, end); // ensure the range is correct
    sig = BufRd.ar(
        numChannels: 2,
        bufnum: buf,
        phase: ptr
    );
    sig = sig * amp;
    Out.ar(out, sig);
}).add;
)
x = Synth.new(\bufrd_sin, [\buf, ~huh.bufnum, \start, 0, \end, ~huh.numFrames - 1, \freq, 0.75]);
x.set(\start, ~huh.numFrames/5, \end, 4*(~huh.numFrames/5));
x.set(\start, 4000, \end, 900);
x.free;
// To hear random sections of the sound file at random speeds, you can use
// a noise generator
(
SynthDef.new(\bufrd_noise, {
    arg amp = 1, out = 0, buf, start, end, freq = 1;
    var sig, ptr;
    ptr = LFDNoise1.ar(freq).range(start, end); // ensure the range is correct
    sig = BufRd.ar(
        numChannels: 2,
        bufnum: buf,
        phase: ptr
    );
    sig = sig * amp;
    Out.ar(out, sig);
}).add;
)
x = Synth.new(\bufrd_noise, [\buf, ~huh.bufnum, \start, 0, \end, ~huh.numFrames - 1, \freq, 1]);
x.set(\freq, 10);
x.set(\freq, 7);
x.free;

// Question: how do I write to buffers?
// RecordBuf and BufWrite.. Look up their help files