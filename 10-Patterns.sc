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
    env = EnvGen.kr(Env.new(
        levels: [0, 1, 0],
        times: [atk, rel],
        curve: [1, -1],
        doneAction: 2
    ));
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


(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pseq([0.6, 0.15, 0.15], inf) // infinite pattern
            .trace, // visualise the pattern in real time
    \freq, Pseq([330, 247, 370], inf)
            .trace, // visualise the pattern in real time
).play;
)
p.stop;