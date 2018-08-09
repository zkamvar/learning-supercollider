//first the Function
{ SinOsc.ar(440, 0, 0.2) }.play;

// now here's an equivalent SynthDef
SynthDef.new("tutorial-SinOsc", { Out.ar(0, SinOsc.ar(440, 0, 0.2)) }).play;

(
SynthDef.new("tutorial-SinOsc-stereo", { var outArray;
    outArray = [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)];
    Out.ar(0, outArray)
}).play;
)


x = { SinOsc.ar(660, 0, 0.2) }.play;
y = SynthDef.new("tutorial-SinOsc", { Out.ar(0, SinOsc.ar(440, 0, 0.2)) }).play;
x.free;    // free just x
y.free;    // free just y


// execute first, by itself
SynthDef.new("tutorial-PinkNoise", { Out.ar(0, PinkNoise.ar(0.3)) }).add;

// then:
x = Synth.new("tutorial-PinkNoise");
y = Synth.new("tutorial-PinkNoise");
x.free; y.free;


s.boot;
(
// send a synth def to server
SynthDef("tpulse", { arg out = 0,freq = 700, sawFreq = 440.0;
    Out.ar(out, SyncSaw.ar(freq, sawFreq, 0.1));
}).add;
)

// Here the defaults for *new will result in a Synth at the head of the default group
// of the default Server. This will use the SynthDef's default arguments;
y = Synth.new("tpulse");
y.free;

// The same done explicitly
y = Synth.new("tpulse", nil, s, \addToHead);
y.free;

// With some arguments
y = Synth.new("tpulse", [\freq, 350, \sawFreq, 220]);
y.free;

// make a new synth
y = Synth("tpulse");

// pause
y.run(false);

y.run(true);

y.set("freq", 5, "sawFreq", 20);

// A grand example
(
// first collect some things to play with
SynthDef("moto-rev", { arg out=0;
    var x;
    x = RLPF.ar(LFPulse.ar(SinOsc.kr(0.2, 0, 10, 21), [0,0.1], 0.1),
        100, 0.1).clip2(0.4);
    Out.ar(out, x);
}).add;

SynthDef("bubbles", { arg out=0;
    var f, zout;
    f = LFSaw.kr(0.4, 0, 24, LFSaw.kr([8,7.23], 0, 3, 80)).midicps;
    zout = CombN.ar(SinOsc.ar(f, 0, 0.04), 0.2, 0.2, 4); // echoing sine wave
    Out.ar(out, zout);
}).add;


SynthDef("rlpf",{ arg out=0,ffreq=600,rq=0.1;
    ReplaceOut.ar( out, RLPF.ar( In.ar(out), ffreq,rq) )
}).add;


SynthDef("wah", { arg out, rate = 1.5, cfreq = 1400, mfreq = 1200, rq=0.1;
    var zin, zout;

    zin = In.ar(out, 2);
    cfreq = Lag3.kr(cfreq, 0.1);
    mfreq = Lag3.kr(mfreq, 0.1);
    rq   = Ramp.kr(rq, 0.1);
    zout = RLPF.ar(zin, LFNoise1.kr(rate, mfreq, cfreq), rq, 10).distort
                    * 0.15;

    // replace the incoming bus with the effected version
    ReplaceOut.ar( out , zout );

}).add;

SynthDef("modulate",{ arg out = 0, freq = 1, center = 440, plusMinus = 110;
    Out.kr(out, SinOsc.kr(freq, 0, plusMinus, center));
}).add;
)

// execute these one at a time

// y is playing on bus 0
y = Synth("moto-rev",["out",0]);

// z is reading from bus 0 and replacing that; It must be *after* y
z = Synth.after(y,"wah",["out",0]);

// stop the wah-ing
z.run(false);

// resume the wah-ing
z.run(true);

// add a rlpf after that, reading and writing to the same buss
x = Synth.after(z,"rlpf",["out",0]);

// create another rlpf after x
t = Synth.after(x,"rlpf",["out",0]);

x.set("ffreq", 400);

x.set(\ffreq, 800); // Symbols work for control names too

// Now let's modulate x's ffreq arg
// First get a control Bus
b = Bus.control(s, 1);

// now the modulator, *before* x
m = Synth.before(x, "modulate", [\out, b]);

// now map x's ffreq to b
x.map("ffreq", b);

m.set("freq", 4, "plusMinus", 20);

x.free;
z.free;
m.free;

// now place another synth after y, on the same bus
// they both write to the buss, adding their outputs
r = Synth.after(y,"bubbles",["out",0]);

y.free;

r.free;

// look at the Server window
// still see 4 Ugens and 1 synth?
// you can't hear me, but don't forget to free me
t.free;

// With Rand, it works!
SynthDef("tutorial-Rand", { Out.ar(0, SinOsc.ar(Rand(440, 660), 0, 0.2)) }).add;
x = Synth("tutorial-Rand");
y = Synth("tutorial-Rand");
z = Synth("tutorial-Rand");
x.free; y.free; z.free;


(
SynthDef("tutorial-args", { arg freq = 440, out = 0;
    Out.ar(out, SinOsc.ar(freq, 0, 0.2));
}).add;
)
x = Synth("tutorial-args");                // no args, so default values
y = Synth("tutorial-args", ["freq", 660]);        // change freq
z = Synth("tutorial-args", ["freq", 880, "out", 1]);    // change freq and output channel
x.free; y.free; z.free;

s.quit;
s.boot;

(
SynthDef.new("tutorial-args", { arg freq = 440, out = 0;
    Out.ar(out, SinOsc.ar(freq, 0, 0.2));
}).add;
)
s.scope; // scope so you can see the effect
x = Synth.new("tutorial-args");
x.set("freq", 660);
x.set("freq", 880, "out", 1);
x.free;


(
  SynthDef.new("zk-square-sin", { |freq = 440|
    Out.ar(0, [SinOsc.ar(freq, 0, 0.2), Pulse.ar(freq / 2) * 0.1]);
  }).add
)
y = Synth.new("zk-square-sin");
z = Synth.new("zk-square-sin");
y.set("freq", 420);
z.set("freq", 422);
y.free; z.free;
