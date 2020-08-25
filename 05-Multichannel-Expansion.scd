// Multichannel expansion is when an array of UGens is translated into multiple
// channels of audio The help document Multichannel Expansion goes into further
// details
s.boot;

// Brining up the level meter can help us visualise the different channels we
// are using ⌘M will do this for us
s.meter;
(
x = {
    [
        SinOsc.ar(300), // Output bus 1
        SinOsc.ar(500)  // Output bus 2
    ]
}.play;
)
x.free;

// This is equivalent to adding an array of frequencies
x = {SinOsc.ar(freq: [300, 500])}.play;
x.free;

// The ugens are basically vectorized, so we can combine them in inutitive ways
// Note: combining ugens of different sizes will have the shorter size wrap
// around
(
x = {
    var sig, amp;
    // Here, the frequency of the left tone fluctuates 7x per second and the
    // right tone 1x per second
    amp = SinOsc.kr(freq: [7, 1]).range(0, 1);
    sig = SinOsc.ar(freq: [300, 500]);
    sig = sig * amp;
}.play;
)
x.free;

// Having more than two busses requires using Mix to include them in the audio
// output Mix crushes everything down into one channel, so we use !2 to copy it
// into two
(
x = {
    var sig, amp;
    // Here, the frequency of the left tone fluctuates 7x per second and the
    // right tone 1x per second
    amp = SinOsc.kr(freq: [7, 1, 2, 0.2, 6]).range(0, 1);
    sig = SinOsc.ar(freq: [300, 500, 700, 900, 1100]);
    sig = sig * amp;
    Mix.new(sig)!(2) * 0.25; // x!2 is equivalent to x.dup(2), which is like
                             // rep(x, 2) in R
}.play;
)
x.free;
// Splay is a bit different in that it adds things to two channels, but
// disperses them evenly between the two channels.
(
x = {
    var sig, amp;
    // Here, the frequency of the left tone fluctuates 7x per second and the
    // right tone 1x per second
    amp = SinOsc.kr(freq: [7, 1, 2, 0.2, 6]).range(0, 1);
    sig = SinOsc.ar(freq: [300, 500, 700, 900, 1100]);
    sig = sig * amp;
    Splay.ar(sig) * 0.5;
}.play;
)
x.free;

// NOTE: duplication acts differently if it's the UGen being duplicated or the
// argument.
y = {PinkNoise.ar(mul: 0.5)!2}.play //identical UGens in both busses
y = {PinkNoise.ar(mul: 0.5!2)}.play //different UGens in both busses

// SynthDefs need to output a UGen. The question is: what do we supply as the
// bus argument for Out.ar()?
//
// A: Use the first channel, and then SC will put the remaining channel into
// the next bus
(
SynthDef.new(\multi, {
    var sig, amp;
    // Here, the frequency of the left tone fluctuates 7x per second and the
    // right tone 1x per second
    amp = SinOsc.kr(freq: [7, 1, 2, 0.2, 6]).range(0, 1);
    sig = SinOsc.ar(freq: [300, 500, 700, 900, 1100]);
    sig = sig * amp;
    sig = Splay.ar(sig) * 0.5;
    Out.ar(bus: 0, channelsArray: sig); // Note, if bus: [0, 1], this will be
                                        // equivalent to writing to busses 0,
                                        // 1, and 2, which will cause a higher
                                        // signal on bus 1.
}).add;
)

x = Synth.new(\multi);
x.free;

// Duplication of randomly generated numbers: nuances

rrand(50, 1200)!4;   // This will give us four copies of the same random value
{rrand(50, 1200)}!4; // by wrapping with {} (a function), it evaluates each time

// Here, we can use this expansion to create our channels
(
SynthDef.new(\multi, {
    var sig, amp;
    // exprand is NOT the best way to go about this because the numbers are
    // generated when the SynthDef is defined, so it will always be the same
    // random frequencies
    amp = SinOsc.kr(freq: {exprand(0.2, 12)}!8).range(0, 1);
    sig = SinOsc.ar(freq: {exprand(50, 1200)}!8);
    sig = sig * amp;
    sig = Splay.ar(sig) * 0.5;
    Out.ar(bus: 0, channelsArray: sig);
}).add;
)

x = Synth.new(\multi);
x.free;

// Using the UGen ExpRand() will allow us to create random values when a new
// synth is created
(
SynthDef.new(\multi, {
    var sig, amp;
    amp = SinOsc.kr(freq: {ExpRand(0.2, 12)}!8).range(0, 1);
    sig = SinOsc.ar(freq: {ExpRand(50, 1200)}!8);
    sig = sig * amp;
    sig = Splay.ar(sig) * 0.5;
    Out.ar(bus: 0, channelsArray: sig);
}).add;
)

x = Synth.new(\multi);
x.free;

// Adding an envelope so we can call several of these to get a tapestry of sine
// waves
(
SynthDef.new(\multi, {
    var sig, amp, env;
    env = EnvGen.kr(
        Env.new([0, 1, 0], times: [10, 10], curve: [1, -1]),
        doneAction: 2
    );
    amp = SinOsc.kr(freq: {ExpRand(0.2, 12)}!8).range(0, 1);
    sig = SinOsc.ar(freq: {ExpRand(50, 1200)}!8);
    sig = sig * amp * env;
    sig = Splay.ar(sig) * 0.5;
    Out.ar(bus: 0, channelsArray: sig);
}).add;
)

x = Synth.new(\multi);

s.quit;
