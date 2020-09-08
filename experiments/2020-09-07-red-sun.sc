(
SynthDef.new(\thing, {
    arg out = 0, freq = 220, width = 0.5, aw = 100;
    var sig, amp, env;
    sig = Pulse.ar(freq: freq, width: width);
    env = EnvGen.kr(Env.perc(aw.reciprocal + 0.001), doneAction: 2);
    amp = SinOsc.kr(freq: aw) * 0.5;
    sig = sig * amp * env;
    sig = Pan2.ar(sig, pos: LFNoise1.kr(freq: 10));
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

~reverbBus = Bus.audio(server: s, numChannels: 2);
~srcGrp = Group.new();
~fxGrp  = Group.after(~srcGrp);
y = Synth.new(\reverb, [\in, ~reverbBus], target: ~fxGrp);


x = Pbind(
    \instrument, \thing,
    \dur,   Prand([1/8, 1/16, 1/32, 1/4, 1/2, 1], inf),
    \aw,    Prand([1, 10, 100, 1000], inf),
    \width, Prand([1/12, 1/8, 1/6, 1/2, 1/3, 1/4, 1], inf),
    \freq,  Prand((Scale.minor.degrees + 59).midicps, inf),
    \out,    ~reverbBus,
    \target, ~srcGrp
).play;
)
x.stop;