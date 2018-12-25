// Three concepts will be discussed:
//
// 1. Nodes
// 2. Busses
// 3. Order of Execution
//
// Because these are all interrelated, they will be discussed using a simplistic
// example of a synth that sends a signal to a reverb generator
s.boot;
s.plotTree;
s.meter;
(
// Sine wave that jumps randomly between 4 partials of a 300hz fundamental
SynthDef.new(\blip, {
    arg out; // bus index to route signal to other synth
    var freq, trig, sig;
    freq = LFNoise0.kr(3).exprange(300, 1200).round(300);
    sig  = SinOsc.ar(freq) * 0.25;
    trig = Dust.kr(density: 2); // random percussive elements
    sig  = sig * EnvGen.kr(Env.perc(attackTime: 0.01, releaseTime: 0.2), gate: trig);
    Out.ar(out, sig);
}).add;

// Reverb receives the signal from blip
SynthDef.new(\reverb, {
    arg in, out = 0; // input and output busses
    var sig;
    sig = In.ar(bus: in, numChannels: 1);
    sig = FreeVerb.ar(sig, mix: 0.5, room: 0.8, damp: 0.2)!2;
    Out.ar(out, sig);
}).add;
)

s.options.numAudioBusChannels
// reset the number of output channels to 4
s.options.numOutputBusChannels = 4;
// reset the number of input channels to 2
s.options.numInputBusChannels = 2;
// Now the channels reserved for hardware are 0 through 5
s.reboot;

// x -> y - ()
//   6    0
y = Synth.new(\reverb, [\in, 6]);
x = Synth.new(\blip, [\out, 6]);
x.free; // x can be freed while y remains to complete the sound.
y.free;

// The above (using an integer to define a bus) is problematic because you don't always know
// where the private audio busses start. It's better to use the Bus.audio method
// This will always choose the lowest available bus that doesn't conflict with the
// audio servers.
~reverbBus = Bus.audio(server: s, numChannels: 1);
~reverbBus.index;

y = Synth.new(\reverb, [\in, ~reverbBus]);
x = Synth.new(\blip, [\out, ~reverbBus]);
x.free; // x can be freed while y remains to complete the sound.
y.free;


