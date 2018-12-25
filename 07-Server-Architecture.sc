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


// Dealing with multichannel signals

(
// Sine wave that jumps randomly between 4 partials of a 300hz fundamental
SynthDef.new(\blip, {
    arg out;
    var freq, trig, sig;
    freq = LFNoise0.kr(3).exprange(300, 1200).round(300);
    sig  = SinOsc.ar(freq) * 0.25;
    trig = Dust.kr(density: 2);
    sig  = sig * EnvGen.kr(Env.perc(attackTime: 0.01, releaseTime: 0.2), gate: trig);
    sig  = Pan2.ar(sig, pos: LFNoise1.kr(freq: 10)); // <------- spread the signal to two channels
    Out.ar(out, sig);
}).add;

// Reverb receives the signal from blip
SynthDef.new(\reverb, {
    arg in, out = 0; // input and output busses
    var sig;
    sig = In.ar(bus: in, numChannels: 2); // <----------------------- Need to use two channels
    sig = FreeVerb.ar(sig, mix: 0.5, room: 0.8, damp: 0.2);//!2; // <- Don't have to expand the output
    Out.ar(out, sig);
}).add;
)

~reverbBus2 = Bus.audio(server: s, numChannels: 2); // SC will set aside two adjacent busses


y = Synth.new(\reverb, [\in, ~reverbBus2]);
x = Synth.new(\blip, [\out, ~reverbBus2]);
x.free; // x can be freed while y remains to complete the sound.
y.free;

// NOTE: It is very possible to have conflicting busses if you accidentally ask for
// something like a single-channel bus for a two-channel synth. That single-channel
// bus will see two channels and shunt it to the next channel over, creating a
// conflict if that bus was already allocated

// Order of Execution -----------------------------
//
// If we use the example above, but switch the synths, we have a problem.
x = Synth.new(\blip, [\out, ~reverbBus2]);
y = Synth.new(\reverb, [\in, ~reverbBus2]);
s.freeAll;

// The way to solve this is to use the target and addAction args:
x = Synth.new(\blip, [\out, ~reverbBus2]);
// Here we specify x as the target and tell SC to add y after x
y = Synth.new(\reverb, [\in, ~reverbBus2], target: x, addAction: \addAfter);

x.free; // free the blip synth, but leave the reverb on the server

x = Synth.before(y, \blip, [\out, ~reverbBus2]);
// This is equivalent
// x = Synth.new(\blip, [\out, ~reverbBus2], target: y, addAction: \addBefore)
x.free;
y.free;

// We can also create groups for the sources and groups for the effects
~srcGrp = Group.new();
~fxGrp  = Group.after(~srcGrp);
x = Synth.new(\blip, [\out, ~reverbBus2], target: ~srcGrp);
y = Synth.new(\reverb, [\in, ~reverbBus2], target: ~fxGrp);
x.free;

// Here, we will add some arguments to the blip synth def to demonstrate the
// importance of using groups: passing group arguments
(
// Sine wave that jumps randomly between 4 partials of a 300hz fundamental
SynthDef.new(\blip, {
    arg out, fund = 300, dens = 2, decay = 0.2;
    var freq, trig, sig;
    freq = LFNoise0.kr(3).exprange(fund, 1200).round(fund);
    sig  = SinOsc.ar(freq) * 0.25;
    trig = Dust.kr(density: dens);
    sig  = sig * EnvGen.kr(Env.perc(attackTime: 0.01, releaseTime: decay), gate: trig);
    sig  = Pan2.ar(sig, pos: LFNoise1.kr(freq: 10));
    Out.ar(out, sig);
}).add;

// Reverb receives the signal from blip
SynthDef.new(\reverb, {
    arg in, out = 0; // input and output busses
    var sig;
    sig = In.ar(bus: in, numChannels: 2);
    sig = FreeVerb.ar(sig, mix: 0.5, room: 0.8, damp: 0.2);
    Out.ar(out, sig);
}).add;
)

// Now we can create 8 instances and place them in the source group
(
8.do {
    Synth.new(\blip, [
        \out, ~reverbBus2,
        \fund, exprand(60, 300).round(30),

    ], target: ~srcGrp);
}
)

// We can now use the group to relay arguments to all nodes inside the group
~srcGrp.set(\decay, 0.01);
~srcGrp.set(\dens, 16);
~srcGrp.set(\dens, 0.25);
~srcGrp.set(\decay, 1.2);
~srcGrp.set(\decay, 5.2);
~srcGrp.freeAll;