s.quit;
s.boot;
f = { [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)] };
// f.play;
// Everything wrapped in {} are functions.
// Arguments are the first part of functions
f = { arg a; a.value + 3 };
f.value(3);
// integers and floats have randomization methods
g = { 12.0.rand; };
f.value(g);

(
// variables MUST be declared at the top of the bloc
var myPower;

// Single-letter variables are okay, though
// Arguments always start with ARG and are separated by
// commas.
f = { arg a, b;
    a - b;
};
f.value(5, 3).postln;
f.value(a: 10, b: 2).postln;

// defaults can be set
myPower = { | x, pow = 2 | // You can also use two pipes for arguments
    var result;
    result = x**pow;
    result;
};

myPower.value(3).postln;
myPower.value(3, pow: 3).postln;
)

(
{             // Open the Function
    SinOsc.ar(    // Make an audio rate SinOsc
        440,      // frequency of 440 Hz, or the tuning A
        0,        // initial phase of 0, or the beginning of the cycle
        0.2)      // mul of 0.2
}.play;        // close the Function and call 'play' on it
)

(
{ var ampOsc;
    ampOsc = SinOsc.kr(0.5, 1.25pi, 0.75, 0.5);
    SinOsc.ar(440, 0, ampOsc);
}.play;
)

// These are equivalent
{ [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)] }.play;
{ SinOsc.ar([440, 442], 0, 0.2) }.play;

(
{ var freq;
    freq = [[660, 880], [440, 660], 1320, 880].choose;
    SinOsc.ar(freq, 0, 0.2);
}.play;
)

{ Pan2.ar(PinkNoise.ar(0.2), SinOsc.kr(0.5)) }.play;
{ Pan2.ar(PinkNoise.ar(0.2), -0.3) }.play; // slightly to the left


{ PinkNoise.ar(0.2) + SinOsc.ar(440, 0, 0.2) + Saw.ar(660, 0.2) }.play;

// one channel
{ Mix.new([SinOsc.ar(440, 0, 0.2), Saw.ar(660, 0.2)]).postln }.play;

// combine two stereo arrays
(
{
    var a, b;
    a = [SinOsc.ar(440, 0, 0.2), Saw.ar(662, 0.2)];
    b = [SinOsc.ar(442, 0, 0.2), Saw.ar(660, 0.2)];
    Mix([a, b]).postln;
}.play;
)

// Look at the post window for frequencies and indices
(
    var n = 8;
    {
        Mix.fill(n, { arg index;
            var freq, rn;
            rn = 4.rand;
            index.postln;
            freq = 440 + index + rn;
            freq.postln;
            SinOsc.ar(freq , 0, 1 / n)
        })
    }.play;
)