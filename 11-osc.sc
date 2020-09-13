(
OSCdef.new(
    \toggle,
    {
        arg msg, time, addr, port;
        x.set(\gate, msg[1]);
    },
    '/main/toggle1'
);
OSCdef.new(
    \fader1,
    {
        arg msg, time, addr, port;
        x.set(\freq, msg[1].linexp(0, 1, 20, 500))
    },
    '/main/fader1'
);
OSCdef.new(
    \fader2,
    {
        arg msg, time, addr, port;
        x.set(\nharm, msg[1].linlin(0, 1, 1, 50));
    },
    '/main/fader2'
);
OSCdef.new(
    \fader3,
    {
        arg msg, time, addr, port;
        x.set(\amp, msg[1].linexp(0, 1, 0.001, 1));
    },
    '/main/fader3'
);
OSCdef.new(
    \rotary1,
    {
        arg msg, time, addr, port;
        x.set(\pan, msg[1].linlin(0, 1, -1, 1));
    },
    '/main/rotary1'
);
OSCdef.new(
    \rotary2,
    {
        arg msg, time, addr, port;
        x.set(\detune, msg[1].linexp(0, 1, 0.01, 12));
    },
    '/main/rotary2'
);

SynthDef.new(\tone, {
    arg freq=40, nharm=12, detune=0.2, gate=0,
        pan=0, amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env.adsr(0.05, 0.1, 0.5, 3), gate);
    sig = Blip.ar(
        freq *
        LFNoise1.kr(0.2!16).bipolar(detune.neg, detune).midiratio,
        nharm
    );
    sig = sig * LFNoise1.kr(0.5!16).exprange(0.1, 1);
    sig = Splay.ar(sig);
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
}).add;
)

x = Synth.new(\tone)
x.free


NetAddr.langPort

(
// Sending messages to the OSC

// This is the address and port for the iPad/iPhone
b = NetAddr.new("192.168.0.7", 90210);
w = Window.new.front;
// This creates a gui slider and number box that responds to the slider.
c = NumberBox(w, Rect(20, 20, 150, 20));
a = Slider(w, Rect(20 , 60, 150, 20))
    .action_({
        c.value_(a.value);
        b.sendMsg('/1/fader2', a.value);
        });
a.action.value;
// This connects the fader of the Simple example in TouchOSC
OSCdef.new(
    \fad2,
    {
        arg msg, time, addr, port;
        // defer is needed here or else we run into weird errors
        {a.valueActionIfChanged_(msg[1])}.defer;
    },
    '/1/fader2'
);
)
