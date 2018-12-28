// Patterns can be quite dense, but they are very practical for all sorts
// of things in SC. Thus, there is an entire tutorial series documented in
// the SC documentation called Pattern Guide with 8 chapters and 8 cookbook
// chapters, and one reference guide :o
//
// There is also Understanding Streams, Patterns and Events

(
SynthDef.new(\sine, {
    arg freq = 440, atk = 0.005, rel = 0.3, amp = 1, pan = 0;
    var sig, env;
    sig = SinOsc.ar(freq: freq);
    env = EnvGen.kr(
        Env.new(
            levels: [0, 1, 0],
            times: [atk, rel],
            curve: [1, -1]
        ),
        doneAction: 2);
    sig = Pan2.ar(sig, pos: pan, level: amp);
    sig = sig * env;
    Out.ar(0, sig);
}).add;
)

// Start with a pattern called Pbind which responds to the play message
// by generating a sequence of events. The most common event type is a
// \note event, which simply generates a synth on the server. Pbind sequences
// these note events.

// This creates a stream of sinusoid notes at a rate of one per second
// in an EventStreamPlayer called p
(
p = Pbind(
    \type, \note,
    \instrument, \sine
).play;
)
p.stop;

// Duration (delta time) can be specified using the \dur key
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, 0.5 // half second duration
).play;
)
p.stop;

// Patterns can be specified using Pseq. Note that Pbind does not care
// if the patterns are all of the same length. It will only run for as
// long as the shortest Pseq
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pseq([0.6, 0.15, 0.15], 4)
            .trace, // visualise the pattern in real time
    \freq, Pseq([330, 247, 370, 220], inf)
            .trace,
).play;
)
p.stop;

// We don't always have to deal in cps (cycles per second), we can use
// midinotes or notes or degrees. These are defined in a hierarchy in the
// Pbind help file. This hierarchy means that we will need to name the
// arguments exactly in our SynthDef
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pseq([0.6, 0.15, 0.15], inf)
            .trace,
    \midinote, Pseq([60, 65, 67, 74], inf)
            .trace,
).play;
)
p.stop;

// Random number generation
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pwhite(0.05, 0.5, inf),  // linear distribution
    \freq, Pexprand(50, 4000, inf) // exponential
            .trace,
    \atk, Pwhite(2.0, 3.0, inf),
    \rel, Pwhite(5.0, 10.0, inf),
    \amp, Pwhite(0.01, 0.2, inf), // lower the amplitude to avoid clipping
    \pan, Pwhite(-0.8, 0.8, inf),
).play;
)
s.plotTree;
p.stop;  