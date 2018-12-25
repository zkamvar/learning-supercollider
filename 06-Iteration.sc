// It's common to take a collection of items and iterate over them, placing them into a function
// SC has a class called Collection, which has subclasses such as Array, Set, Bag, etc.
//
// iterations are methods of Collections. The most general method is the do method.
// The do method evaluates a function for each item in the collection
//
// Here, we are writing the squares of the array into a new array called x.
(
x = Array.newClear(5);
[6, 4, 0.5, 10, 7].do{ // do will always return its original input
    arg item, count;
    x[count] = item.squared;
};
)
x

// A better way to do this is to use the collect iterator
(
z = [6, 4, 0.5, 10, 7].collect {
    arg item;
    item.squared;
}
)

// Note that 5.do is equivalent to [0, 1, 2, 3, 4].do

s.boot;
// boring
x = {VarSaw.ar(freq: 40!2, iphase: 0, width: 0.05)}.play;
x.free;

// Adding random subfrequencies to the tones, it's much more interesting
(
SynthDef.new(\iter, {
    arg freq = 40;
    var temp, sum, env;
    env = EnvGen.kr(
        Env.perc(attackTime: 0.01, releaseTime: 5, level: 1, curve: -2),
        doneAction: 2
    );
    sum = 0;
    10.do {
        temp = VarSaw.ar(
            freq: freq * {Rand(0.99, 1.02)}!2,
            iphase: {Rand(0.0, 1.0)}!2, // 0, having the same initial phase offset creates clipping
            width: {ExpRand(0.005, 0.05)}!2
        );
        sum = sum + temp;
    };
    sum = sum * env * 0.05;
    Out.ar(0, sum);
}).add;
)

// Convert midi notes to cycles per second
Synth.new(\iter, args: [\freq, 66.midicps]);
Synth.new(\iter, args: [\freq, 73.midicps]);
Synth.new(\iter, args: [\freq, 80.midicps]);
Synth.new(\iter, args: [\freq, 75.midicps]);

// This time, we'll incorporate the audio manipulation count in the SynthDef
(
SynthDef.new(\iter2, {
    arg freq = 200, dev = 1.02;
    var temp, sum;
    sum = 0;
    10.do { // NOTE: you cannot change this iteration with an arg. It must be changed manually
        arg count; // note that we do not need a second arg here because it's redundant
        temp = SinOsc.ar(
            freq *
            (count + 1) *
            LFNoise1.kr({Rand(0.05, 0.2)}!2).range(dev.reciprocal, dev) // have the partials fluctuate randomly
        );
        temp = temp * LFNoise1.kr({Rand(0.05, 8)}!2).exprange(0.01, 1); // have the amplitud fluctuate randomly
        sum  = sum + temp;
    };
    sum = sum * 0.05;
    Out.ar(0, sum);
}).add;
)

x = Synth.new(\iter2);
x.set(\freq, 26.midicps);
x.set(\freq, 31.midicps);
x.set(\dev, 1.06);
x.set(\dev, 1.01);
x.set(\dev, 1.51);
x.free;