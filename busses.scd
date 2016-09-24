s.boot;
In.ar(0, 1); // this will return 'an OutputProxy'
In.ar(0, 4); // this will return an Array of 4 OutputProxies

(
SynthDef("tutorial-args", { arg freq = 440, out = 0;
    Out.ar(out, SinOsc.ar(freq, 0, 0.2));
}).add;
)
s.scope;
// both write to bus 1, and their output is mixed
x = Synth("tutorial-args", ["out", 1, "freq", 660]);
y = Synth("tutorial-args", ["out", 1, "freq", 770]);

// ALLOCATING BUSSES
/*
Busses can be audio out or control out. AFAIK, sc allows you
to have as many busses as you want.

This is a way of setting a group of outputs.

The first two busses in Out.ar() are the left and right audio
channels. Private busses are audio chanels that you can control.
I believe these can be used for routing synths and control modules
together.
*/
b = Bus.control(s, 2); // Get a two channel control Bus
c = Bus.audio(s);     // Get a one channel private audio Bus (one is the default)

// Examples of how to use busses.
s.reboot;
b = Bus.control(s, 2); // a 2 channel control Bus
b.index;               // this should be zero
b.numChannels          // Bus also has a numChannels method
c = Bus.control(s);
c.numChannels;         // the default number of channels is 1
c.index;               // note that this is 2; b uses 0 and 1
b.free; c.free;


(
  // Uses a bus object as input and uses that to modulate the
  // Frequency of the incoming control oscillator.
SynthDef("tutorial-Infreq", { arg bus, freqOffset = 0;
    // this will add freqOffset to whatever is read in from the bus
    Out.ar(0, SinOsc.ar(In.kr(bus) + freqOffset, 0, 0.5));
}).add;

SynthDef("tutorial-Outfreq", { arg freq = 400, bus;
    Out.kr(bus, SinOsc.kr(1, 0, freq/40, freq));
}).add;

b = Bus.control(s,1);
)
// tutorial-Outfreq(freq) -> tutorial-Infreq(freqOffset) -> Sound
s.scope;
(
x = Synth.new("tutorial-Outfreq", [\bus, b]);
// Both of these below write to the same out bus.
y = Synth.after(x, "tutorial-Infreq", [\bus, b]);
z = Synth.after(x, "tutorial-Infreq", [\bus, b, \freqOffset, 200]);
)
x.free; y.free; z.free; b.free;

/* ===============================================================
LARGE TUTORIAL

This is an example of how to utilize audio busses. It creates a
weird kind of tapping with pink noise, random sin moans with
reverb.
=============================================================== */

(
// the arg direct will control the proportion of direct to processed signal
SynthDef("tutorial-DecayPink", { arg outBus = 0, effectBus, direct = 0.5;
    var source;
    // Decaying pulses of PinkNoise. We'll add reverb later.
    source = Decay2.ar(Impulse.ar(1, 0.25), 0.01, 0.2, PinkNoise.ar);
    // this will be our main output
    Out.ar(outBus, source * direct);
    // this will be our effects output
    Out.ar(effectBus, source * (1 - direct));
}).add;

SynthDef("tutorial-DecaySin", { arg outBus = 0, effectBus, direct = 0.5;
    var source;
    // Decaying pulses of a modulating sine wave. We'll add reverb later.
    source = Decay2.ar(Impulse.ar(0.3, 0.25), 0.3, 1, SinOsc.ar(SinOsc.kr(0.2, 0, 110, 440)));
    // this will be our main output
    Out.ar(outBus, source * direct);
    // this will be our effects output
    Out.ar(effectBus, source * (1 - direct));
}).add;

SynthDef("tutorial-Reverb", { arg outBus = 0, inBus;
    var input;
    input = In.ar(inBus, 1);

    // a low-rent reverb
    // aNumber.do will evaluate its function argument a corresponding number of times
    // {}.dup(n) will evaluate the function n times, and return an Array of the results
    // The default for n is 2, so this makes a stereo reverb
    16.do({ input = AllpassC.ar(input, 0.04, { Rand(0.001,0.04) }.dup, 3)});

    Out.ar(outBus, input);
}).add;

b = Bus.audio(s, 1); // this will be our effects bus
)

(
x = Synth.new("tutorial-Reverb", [\inBus, b]);
y = Synth.before(x, "tutorial-DecayPink", [\effectBus, b]);
z = Synth.before(x, "tutorial-DecaySin", [\effectBus, b, \outBus, 1]);
)
s.scope;
// Change the balance of wet to dry
y.set(\direct, 1); // only direct PinkNoise
z.set(\direct, 1); // only direct Sine wave
y.set(\direct, 0); // only reverberated PinkNoise
z.set(\direct, 0); // only reverberated Sine wave
x.free; y.free; z.free; b.free;
