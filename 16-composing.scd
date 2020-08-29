// Note: this picks up from where episode 15 left off, so I will go ahead and
// copy over the previous synthdefs
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), // Envelope arguments
    freq=500,    detune=0.2,  // Waveform arguments
    cfmin=500,   cfmax=2000,  // bandpass filter argumetns
    cfhzmin=0.1, cfhzmax=0.3, // ...
    rqmin=0.1,   rqmax=0.2,   // ...
    lsf=200,     ldb=0,       // Low shelf filter arguments
    pan=0,                    // *hook voice* PAN?
    amp=1,       out=0;       // amplitude and output channel
    var sig, env;
    // --- envelope generator (attack, sustain, release)
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- Oscillator (generates the signal) /\/\/\/\/
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    // --- bandpass filter
    sig = BPF.ar(
      sig,
      // center frequency of the bandpass filter (what gets through)
      {
        // Generates random center frequencies in [cfmin, cfmax] ...
        LFNoise1.kr(
          // ... at a rate between cfhzmin and cfhzmax.
          LFNoise1.kr(4).exprange(cfhzmin, cfhzmax)
        ).exprange(cfmin, cfmax)
      }!2,
      // reciprocal quality: lower is a stronger BP effect
      // Generates random quality value in [rqmin, rqmax]
      {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    // --- low shelf filter
    sig = BLowShelf.ar(sig, lsf, 0.5, ldb);
    // --- channel balancer
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)
// # 16 Composing a Piece: Part 2
//
// The synthdef above will geneate a neat little synthesizer, but cannot process
// sound files. We want to create a synthdef that will play a buffer, apply an
// amplitude envelope and stereo panning and route the sound through a bandpass
// filter.
(// https://stackoverflow.com/a/18938315/2752888
~here = if (Platform.ideName == "scqt",       // Test if we are in scide
    { thisProcess.nowExecutingPath.dirname }, // only works interactively
    { "/Users/zhian/Documents/Learning/supercollider/learning-supercollider/" }                           // assume we used sclang -p $(pwd)
)
)

(
  SynthDef(\bpfbuf, {
    arg atk=0, sus=0, rel=3, c1=1, c2=(-1),
        buf=0, rate=1, spos=0,
        freq=440, rq=1, bpfmix=0,
        pan=0, amp=1, out=0;
    var sig, env;
    // --- Flat envelope generator with controls for curves of atk and sustain
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- All of our sound files are MONO, so the first arg is 1
    //     Note that the ir method of BufRateScale is more efficient because we
    //     know that we will not be chaning the bufnum while the synth is running.
    sig = PlayBuf.ar(2, buf, rate: rate * BufRateScale.ir(buf), startPos: spos);
    // --- Two-channel cross-fade between the raw signal and the BPF signal.
    //     For the BPF, we are giving the reciprocal between quality and
    //     amplitude so that we don't sacrifice loudness for quality.
    sig = XFade2.ar(sig, BPF.ar(sig, freq, rq, mul: 1/rq.sqrt), pan: bpfmix*2-1);
    sig = sig * env;
    sig = Pan2.ar(sig, pan, level: amp);
    Out.ar(out, sig);
  }).add;
)


~b1 = Buffer.read(s, ~here +/+ "sounds/bubbles/bubbles01.aiff")
~b2 = Buffer.read(s, ~here +/+ "sounds/bubbles/bubbles02.aiff")
~b3 = Buffer.read(s, ~here +/+ "sounds/bubbles/bubbles03.aiff")
~b1.play;
~b2.play;
~b3.play;

// Playing the buffer is straightforward
Synth(\bpfbuf, [\buf, ~b2.bufnum]);
(
// We can also choose a random 0.1 second bit from the buffer
Synth(\bpfbuf,
    [
        \buf, ~b2.bufnum,
        \rel, 0.1,
        \spos, rrand(0, ~b2.numFrames/2)
    ]
);
)
// If we shift the BPF mix to entirely mixed, then we can hear cool things
(
Synth(\bpfbuf,
    [
        \buf, ~b2.bufnum,
        \atk, 2,
        \sus, 1,
        \rel, 2,
        \bpfmix, 1,
        \freq, 2000,
        \rq, 1/50,
        \amp, 2,
        \rate, 0.25,
    ]
);
)
// Randomizing
(
rrand(4,10).do {
    Synth(\bpfbuf,
        [
            \buf, [~b1, ~b2, ~b3].choose,
            \atk, exprand(2, 3),
            \sus, 1,
            \rel, exprand(2, 3),
            \bpfmix, 1,
            \freq, (Scale.lydian.degrees+69).choose.midicps * [1,2].choose,
            \rq, exprand(0.001, 0.008),
            \amp, exprand(1, 3),
            \rate, exprand(0.4, 1.2),
            \pan, rrand(-0.5, 0.5),
        ]
    );
}
)

// We want to read in a bunch of files to use, and they are in a different
// folder. To do this, we can use a dictionary to store the sounds in each
// directory so that we know what we are referencing.
(
var add_entries;
~here = if (Platform.ideName == "scqt",       // Test if we are in scide
    { thisProcess.nowExecutingPath.dirname }, // only works interactively
    { "/Users/zhian/Documents/Learning/supercollider/learning-supercollider/" }                           // assume we used sclang -p $(pwd)
);

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
b = Dictionary.new;
PathName(~here +/+ "sounds").entries.do{
    arg subfolder;
    if (subfolder.isFolder) {
        add_entries.(subfolder, "sounds");
    } {
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
b[\sounds_bubbles][1].play;
b[\sounds_huhuh][0].play;
b[\sounds_bubbles_hi][0].play;
b[\sounds_bubbles_low][0].play;
b[\sounds_peanut][0].play;
b[\sounds_snaps][4].play;

(
rrand(4,10).do {
    Synth(\bpfbuf,
        [
            \buf, b[\sounds_bubbles][rand(0, b[\sounds_bubbles].size)],
            \atk, exprand(2, 3),
            \sus, 1,
            \rel, exprand(2, 3),
            \bpfmix, 1,
            \freq, (Scale.lydian.degrees+69).choose.midicps * [1,2].choose,
            \rq, exprand(0.001, 0.008),
            \amp, exprand(1, 3),
            \rate, exprand(0.4, 1.2),
            \pan, rrand(-0.5, 0.5),
        ]
    );
}
)

// We'll make another synthdef to process the signals... a reverb effect from
// scratch
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), // Envelope arguments
    freq=500,    detune=0.2,  // Waveform arguments
    cfmin=500,   cfmax=2000,  // bandpass filter argumetns
    cfhzmin=0.1, cfhzmax=0.3, // ...
    rqmin=0.1,   rqmax=0.2,   // ...
    lsf=200,     ldb=0,       // Low shelf filter arguments
    pan=0,                    // *hook voice* PAN?
    amp=1,       out=0;       // amplitude and output channel
    var sig, env;
    // --- envelope generator (attack, sustain, release)
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- Oscillator (generates the signal) /\/\/\/\/
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    // --- bandpass filter
    sig = BPF.ar(
      sig,
      // center frequency of the bandpass filter (what gets through)
      {
        // Generates random center frequencies in [cfmin, cfmax] ...
        LFNoise1.kr(
          // ... at a rate between cfhzmin and cfhzmax.
          LFNoise1.kr(4).exprange(cfhzmin, cfhzmax)
        ).exprange(cfmin, cfmax)
      }!2,
      // reciprocal quality: lower is a stronger BP effect
      // Generates random quality value in [rqmin, rqmax]
      {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    // --- low shelf filter
    sig = BLowShelf.ar(sig, lsf, 0.5, ldb);
    // --- channel balancer
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;

SynthDef(\bpfbuf, {
    arg atk=0, sus=0, rel=3, c1=1, c2=(-1),
        buf=0, rate=1, spos=0,
        freq=440, rq=1, bpfmix=0,
        pan=0, amp=1, out=0;
    var sig, env;
    // --- Flat envelope generator with controls for curves of atk and sustain
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- All of our sound files are MONO, so the first arg is 1
    //     Note that the ir method of BufRateScale is more efficient because we
    //     know that we will not be chaning the bufnum while the synth is running.
    sig = PlayBuf.ar(2, buf, rate: rate * BufRateScale.ir(buf), startPos: spos);
    // --- Two-channel cross-fade between the raw signal and the BPF signal.
    //     For the BPF, we are giving the reciprocal between quality and
    //     amplitude so that we don't sacrifice loudness for quality.
    sig = XFade2.ar(sig, BPF.ar(sig, freq, rq, mul: 1/rq.sqrt), pan: bpfmix*2-1);
    sig = sig * env;
    sig = Pan2.ar(sig, pan, level: amp);
    Out.ar(out, sig);
  }).add;

// What IS reverb?
//
// it's the result of sound waves propgating in an enclosed space. It's the sum
// of many different feedback delays. It's a balance between dry and wet.
SynthDef(\reverb, {
    arg in, predelay=0.1, revtime=1.8, lpf=4500, mix=0.15, amp=1, out=0;
    var dry, wet, temp, sig; // temp is [temp]orary
    dry  = In.ar(in, 2);
    temp = In.ar(in, 2);
    wet = 0;
    // DelayN creates the room size with No interpolation
    temp = DelayN.ar(temp, maxdelaytime: 0.2, delaytime: 2, mul: predelay);
    // Create the delayed signals. Since we are dealing with fixed delay times,
    // we do not need or want interpolation here.
    16.do{
        temp = AllpassN.ar(temp, 0.05, {Rand(0.001, 0.05)}!2, revtime);
        // In a lot of realworld cases, high frequencies get absorbed more quickly
        // than lower frequencies. Dampening like this can be acheived with a
        // Lowpass filter
        temp = LPF.ar(temp, freq: lpf);
        // add temporary delay line to the wet signal.
        wet = wet + temp;
    };
    sig = XFade2.ar(dry, wet, mix*2-1, amp);
    Out.ar(out, sig);
}).add;
)

// Reserving a 2-channel audio bus for the reverb
~reverbBus = Bus.audio(s, 2);
// Creating the reverb synth FIRST so that way it's at the bottom of the stack
//
// Whenever sending a signal from one synth to another synth, the sending synth
// MUST be above the recieving synth in the node chain
~reverbSynth = Synth(\reverb, [\in, ~reverbBus]);

// This creates the \bpfbuf synth on top of the reverb synth.
(
Synth(
    \bpfbuf,
    [
        \buf, b[\sounds_bubbles][(0..2).choose],
        \rel, rrand(0.15, 0.25),
        \rate, rrand(-2.0, 3.0).midiratio,
        \out, ~reverbBus,
    ]
);
)

// We can create a pattern using the bubbles and some randomness for
// duration
(
p = Pbind(
    \instrument, \bpfbuf,
    \dur, Pexprand(0.1, 1),
    \buf, Pxrand(b[\sounds_bubbles][(0..2)]++b[\sounds_peanut][(0..6)], inf),
    \rel, Pexprand(0.01, 0.5),
    // \spos, Pwhite(1000, 40000),
    \rate, Pwhite(-2.0, 3.0).midiratio,
    \amp, Pexprand(0.5, 0.9),
    \out, Prand([~reverbBus, 0], inf),
).play;
)
~reverbSynth.set(\mix, 0.5);

p.stop;

// If we use cmd+period, the reverb synth disappears. To avoid this, we can use
// ServerTree to reinstantiate the reverb whenever we clear the synths.
~createReverb = {~reverbSynth = Synth(\reverb, [\in, ~reverbBus])};
ServerTree.add(~createReverb); // magic!

// remove all registered commands
ServerTree.removeAll

