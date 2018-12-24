// From http://distractionandnonsense.com/sc/A_Practical_Guide_to_Patterns.pdf
(
  TempoClock.default.tempo = 84/60;
p = Pbind(\scale, #[0, 2, 3, 5, 7, 8, 10],
          \root, 2,
          \degree, Pseq (#[rest, 4, 3, 4, 2, 4, 1, 4, 0, 4, -0.9, 4, 0, 4, 1, 4,
                          2, 4, -3, 4, -1.9, 4, -0.9, 4, 0, 4, -0.9, 4, 0, 4, 1,
                          4, 2], 1),
          \dur, 0.25 ).play;
)
(
p = Pbind(\scale, #[0, 2, 3, 5, 7, 8, 10],
          \root, 2,
          \degree, Place ([#[rest, 3, 2, 1, 0, -0.9, 0, 1, 2, -3, -1.9, -0.9 ,
                           0, -0.9, 0, 1, 2], (4 ! 16) ++ \rest], 17),
           \dur, 0.25 ).play;
)
(
p = Pbind(\scale, #[0, 2, 3, 5, 7, 8, 10],
          \root, 2,
          \degree, Ppatlace([Pseq(#[rest, 3, 2, 1, 0, -0.9, 0, 1, 2, -3, -1.9,
                             -0.9, 0, -0.9, 0, 1, 2], 1),
                             Pn (4, 16)], inf),
          \dur, 0.25 ).play;
)


// from http://superdupercollider.blogspot.com/2009/02/simple-drum-machine.html?showComment=1376788840893#c7697891275370111434
(
SynthDef(\kick, {|out = 0, amp = 0, pan|
var env, bass;
env = EnvGen.kr(Env.perc(0.001, 0.2, 1, -4), 1, doneAction:2);
bass = SinOsc.ar(80) + Crackle.ar(1, 0.5);
Out.ar(out, Pan2.ar(bass*env, pan, amp));
}).add;

SynthDef(\snare, {|out = 0, amp = 0, pan|
var env, snare;
env = EnvGen.kr(Env.perc(0.001, 0.1, 1, -5), 1, doneAction:2);
snare = SinOsc.ar(120) - WhiteNoise.ar(0.5, 0.5);
Out.ar(out, Pan2.ar(snare*env, pan, amp));
}).add;

SynthDef(\hat, {|out = 0, amp = 0, pan|
var env, hat;
env = EnvGen.kr(Env.perc(0.002, 0.3, 1, -2), 1, doneAction:2);
hat = Klank.ar(`[ [ 6563, 9875 ],
[ 0.6, 0.5 ],
[ 0.002, 0.003] ], PinkNoise.ar(1));
Out.ar(out, Pan2.ar(hat*env, pan, amp));
}).add;

SynthDef(\tom, {|out = 0, amp = 0, pan|
var env, tom;
env = EnvGen.kr(Env.perc(0.001, 0.1, 1, -5), 1, doneAction:2);
tom = SinOsc.ar(440);
Out.ar(out, Pan2.ar(tom*env, pan, amp));
}).add;
)

(
a = Pseq ([1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0], 5);
b = Pseq ([0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0], 5);
c = Pseq ([0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0], 5);
d = Pseq ([0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1], 5);


p = Ppar(
    [a, b, c, d].collect { |pattern, i|
        Pbind(
            \instrument, [\kick, \snare, \hat, \tom].at(i),
            \dur, 0.30,
            \amp, 0.5,
            \noteOrRest, Pif(pattern > 0, 1, Rest())
        )
    }
).play;
)