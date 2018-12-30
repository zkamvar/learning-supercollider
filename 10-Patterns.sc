// Patterns can be quite dense, but they are very practical for all sorts
// of things in SC. Thus, there is an entire tutorial series documented in
// the SC documentation called Pattern Guide with 8 chapters and 8 cookbook
// chapters, and one reference guide :o
//
// There is also Understanding Streams, Patterns and Events
s.boot;
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
s.plotTree;
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

// Random number generation: sounds spooky and shows the power of this
// fully operational space station
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pwhite(0.05, 0.5, inf),  // linear distribution
    \freq, Pexprand(50, 4000, inf) // exponential
            .trace,
    \atk, Pwhite(2.0, 3.0, inf),
    \rel, Pwhite(5.0, 10.0, inf),
    \amp, Pexprand(0.01, 0.2, inf), // lower the amplitude to avoid clipping
    \pan, Pwhite(-0.8, 0.8, inf),
).play;
)
s.plotTree;
p.stop;

// Let's say we want these to fall in line with the harmonic series
// We can achieve this with the "round" method
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pwhite(0.05, 0.5, inf),
    \freq, Pexprand(50, 4000, inf).round(55).trace,
    \atk, Pwhite(2.0, 3.0, inf),
    \rel, Pwhite(5.0, 10.0, inf),
    \amp, Pexprand(0.01, 0.2, inf),
    \pan, Pwhite(-0.8, 0.8, inf),
).play;
)

p.stop;

// This can also be achieved using midinote and harmonic
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pwhite(0.05, 0.5, inf),
    \midinote, 33,  // partials of midinote 33
    \harmonic, Pexprand(1, 80, inf).round.trace,
    \atk, Pwhite(2.0, 3.0, inf),
    \rel, Pwhite(5.0, 10.0, inf),
    \amp, Pexprand(0.01, 0.2, inf),
    \pan, Pwhite(-0.8, 0.8, inf),
).play;
)

p.stop;

// We can also control the amplitude of the partials by using Pkey
(
p = Pbind(
    \type, \note,
    \instrument, \sine,
    \dur, Pwhite(0.05, 0.5, inf),
    \midinote, 33,
    \harmonic, Pexprand(1, 80, inf).round.trace,
    \atk, Pwhite(2.0, 3.0, inf),
    \rel, Pwhite(5.0, 10.0, inf),
    \amp, Pkey(key: \harmonic).reciprocal * 0.3, // emphasize lower notes
    \pan, Pwhite(-0.8, 0.8, inf),
).play;
)

p.stop;

// If we want to manipulate the pattern in real time, we use a Pdef

(
Pdef( // Any field here can be modified and re-run in real time without
      // interrupting the stream.
    \sinepat,
    Pbind(
        \type, \note,
        \instrument, \sine,
        \dur, Pwhite(0.05, 0.5, inf),
        \midinote, Pseq([40], inf).trace,
        \harmonic, Pexprand(1, 80, inf).round,
        \atk, Pwhite(2.0, 3.0, inf),
        \rel, Pwhite(5.0, 10.0, inf),
        \amp, Pkey(key: \harmonic).reciprocal * 0.3, // emphasize lower notes
        \pan, Pwhite(-0.8, 0.8, inf),
    );
).play; // replace with stop when finished
)

// Beats --------------------------------------------------------------

(// https://stackoverflow.com/a/18938315/2752888
~here = if (Platform.ideName == "scqt",       // Test if we are in scide
    { thisProcess.nowExecutingPath.dirname }, // only works interactively
    { File.getcwd }                           // assume we used sclang -p $(pwd)
);

// Loading bubble sounds into a dictionary where we will sort them by
// low, mid, and hi sounds (roughly).
d = Dictionary.new;
d.free;

d.add(\l -> PathName(~here +/+ "sounds/bubbles/low")
    .entries
    .collect({ |sf|
      Buffer.read(server: s, path: sf.fullPath);
    });
);
d.add(\m -> PathName(~here +/+ "sounds/bubbles/mid")
    .entries
    .collect({ |sf|
      Buffer.read(server: s, path: sf.fullPath);
    });
);
d.add(\h -> PathName(~here +/+ "sounds/bubbles/hi")
    .entries
    .collect({ |sf|
      Buffer.read(server: s, path: sf.fullPath);
    });
);
)

/* Test the sounds out
d[\l].choose.play; 
d[\m].choose.play;
d[\h].choose.play;
*/

// Now we can create a synth that will play our buffer dictionary

(
SynthDef.new(\bufplay, { 
  arg buf = 0, rate = 1, amp = 1;
  var sig;
  sig = PlayBuf.ar(
    numChannels: 2, 
    bufnum: buf, 
    rate: BufRateScale.ir(buf) * rate,
    doneAction: 2
  );
  sig = sig * amp;
  Out.ar(0, sig);
}).add;
)

/* Synth.new(\bufplay, [\buf, d[\l].choose.bufnum]) */

(
Pdef(
  \rhythm,
  Pbind(
    \instrument, \bufplay,
    \dur, Pseq([1/16], inf),
    \stretch, 1.875, // 60/128 * 4,
    \buf, Pxrand(d[\l]++d[\h]++d[\m], inf),
    \rate, 1,
    \amp, 0.5,
  );
).stop;
)
