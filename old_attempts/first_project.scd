s.quit;
s.boot;
s = Server.default;


// but its easy to make a server window
s.makeWindow;
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
s.scope;

// PLOTTING
// The plot function allows me to see what the waves look like
{ PinkNoise.ar(0.2) + SinOsc.ar(440, 0, 0.2) + Saw.ar(660, 0.2) }.plot();

{ PinkNoise.ar(0.2) + SinOsc.ar(440, 0, 0.2) + Saw.ar(660, 0.2) }.plot();

// An oscilloscope
{ PinkNoise.ar(0.2) + SinOsc.ar(440, 0, 0.2) + Saw.ar(660, 0.2) }.scope;

// Multiple channels
{ [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)] }.scope;

// Zooming
{ [SinOsc.ar(440, 0, 0.2), SinOsc.ar(442, 0, 0.2)] }.scope(zoom: 5);

SinOsc
Function
// Using an environment
(
f = { |a, b| (a * b).postln };

Environment.use({
  ~a = 3;
  ~b = 10;
  f.valueEnvir;
});
)

// Loops and clocks
t = Task({ { "I'm loopy".postln; 1.wait;}.loop });
t.start;
t.stop;

{ "2 seconds have passed.".postln; }.defer(2);

(
{ "chicken".postln }.defer(0); // schedules on the AppClock
{ "egg".postln }.defer // evaluates immediately
)

(
fork { // schedules on a TempoClock
    { "chicken".postln }.defer // schedules on the AppClock
};
{ "egg".postln }.defer // evaluates immediately
)


(
var i, x, z;
z = [0, 1, 1.1, 1.3, 1.5, 2];
i = z.choose;
x = case
    { i == 1 }   { \no }
    { i == 1.1 } { \wrong }
    { i == 1.3 } { \wrong }
    { i == 1.5 } { \wrong }
    { i == 2 }   { \wrong }
    { i == 0 }   { \true };
x.postln;
)


// a strange mapping
(
var f = {|degree, stepsPerOctave, acc|
    (1.8 ** (degree % stepsPerOctave) + acc).postln
};
Pbind(
    \scale, f,
    \degree, Pseq([0, 1, 2b, 3s, 4s, 6, 14, [0, 2, 4], [1, 3, 6]], inf)
).play
)

x = { |freq = 440| SinOsc.ar(freq, 0, 0.3) }.play; // this returns a Synth object;
x.set(\freq, 880); // note you can set the freq argument
x.defName; // the name of the resulting SynthDef (generated automatically in a cycle of 512)
x.release(4); // fadeout over 4 seconds

{ SinOsc.ar(440, 0, 0.3) }.play(fadeTime: 0.0);
Array

Array.fill2D(2, 4, 0);
Array.fill2D(3, 4, { arg r, c; r*c+c; });

Array.geom(5, pi/3, 3).postln;

Array.series(5, 10, 2).postln;


x = [ 1, 2, 3];
8.do({|i| x.permute(i).postln;});
