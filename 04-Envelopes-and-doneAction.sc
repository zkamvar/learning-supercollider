// Many times we want a synth to fade in and fade out.
// This can be accomplished with Envelope Ugens
s.boot;
s.plotTree; // Visualise the active synths
(
// Here, we create and play an anonymous synth.
// The doneAction: 2 indicates that SC should free the synth
// Once it is finished.
{
    var sig, freq, env;
    // Using XLine is an expoential version. This gives a more
    // "Natural" sound. We would use this UNLESS we are specifying
    // Amplitude in decibles. If we use decibles (a linear scale),
    // Then we would convert with the dbamp method
    //
    // If there are two envelopes, then the one with the shorter duration will
    // execute its doneAction first, so here we set the doneAction to 0
    env = XLine.kr(start: 1, end: 0.01, dur: 3, doneAction: 0);
    // Frequency is also preceived on an exponential scale.
    freq = XLine.kr(start: 880, end: 110, dur: 5, doneAction: 2);
    sig = Pulse.ar(freq) * env; //env.dbamp; // convert from db to amp
}.play;
)


// EnvGens ----------------------------------------------------------------------
//
// All EnvGens need an Env. The starting point is a triangluar Env
//    levels: [0, 1, 0] // start at zero, go to one, end at zero
//    times:   [1, 1]   // durations between each level
//    curve:  'lin'     // linearly interpolate between points
// Env.new.plot;
( // Default env: A traingle. This will increase the amplitude from zero to one
  // and back to zero in two seconds.
{
    var sig, env;
    env = EnvGen.kr(Env.new, doneAction: 2);
    sig = Pulse.ar(ExpRand(30, 500)) * env
}.play;
)

// Our own env. We can set something like
Env.new(levels: [0, 1, 0.2, 0], times: [0.5, 1, 2]).plot;
// Exponential can't interpolate if it includes zero
Env.new(levels: [0.001 , 1, 0.2, 0.001], times: [0.5, 1, 2], curve: \exp).plot;
// We can also have an array specifying the curvature for each time.
// Positive: slow then quick
// Negative: quick then slow
// zero: linear
Env.new(levels: [0, 1, 0.2, 0], times: [0.5, 1, 2], curve: [3, -3, 0]).plot;
// further from zero is more extreme
Env.new(levels: [0, 1, 0.2, 0], times: [0.5, 1, 2], curve: [13, -13, 0]).plot;
// Reversing gives us a different shape
Env.new(levels: [0, 1, 0.2, 0], times: [0.5, 1, 2], curve: [-3, 3, 0]).plot;
// We can replace numbers with symbols
Env.new(levels: [0, 1, 0.2, 0], times: [0.5, 1, 2], curve: [\sine, \sine, 0]).plot;

// Here's what it sounds like when we create the envelope with our custom levels
(
{
    var sig, env;
    env = EnvGen.kr(Env.new(
        levels: [0, 1, 0.2, 0],
        times:  [0.5, 1, 2],
        curve:  [3, -3, 0]),
    doneAction: 2);
    sig = Pulse.ar(ExpRand(30, 500)) * env
}.play;
)
// A gate can be used as a trigger. This will trigger as soon as it goes above 0.
// The only issue here is that the gate needs to be reset.
// If we use t_gate, then it will reset itself after the cycle is complete.
//
// This is a persistant synth because doneAction: 0
(
x = {
    arg t_gate=0;
    var sig, env;
    env = EnvGen.kr(Env.new(
        levels: [0, 1, 0.2, 0],
        times:  [0.5, 1, 2],
        curve:  [3, -3, 0]),
    gate: t_gate,
    doneAction: 0); // We want this to be re-triggerable:
    sig = Pulse.ar(LFPulse.kr(8).range(600, 900)) * env
}.play;
)
x.set(\t_gate, 1);

// Using an adsr envelope (Attak, Decay, Sustain, Release) with gate
(
x = {
    arg gate=0;
    var sig, env;
    env = EnvGen.kr(Env.adsr(
        attackTime: 0.02,
        decayTime: 0.2,
        sustainLevel: 0.25,
        releaseTime: 1,
        peakLevel: 1,
        curve: -4
    ),
    gate: gate, // Gate is preferable to t_gate since we want the sustain
    doneAction: 2); // we only want this to go on for the duration
    sig = VarSaw.ar(SinOsc.kr(16).range(500, 1000)) * env;
}.play;
)
x.set(\gate, 1);

x.set(\gate, 0);
Env.adsr(0.02, 0.2, 0.25, 1, 1, -4).test(2).plot;


s.quit;