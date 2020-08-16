// Composition strategies for creating interesting sound
s.boot:
s.meter;
s.plotTree;

// # Composition
//
// Almost always begins with a synthdef
(
  // This synthdef is a sawtwooth wave run through a bandpass filter
  // An envelope generator allows us to shape the sound over time 
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), freq=500, cf=1500, rq=0.2, amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    sig = Saw.ar(freq);
    sig = BPF.ar(sig, cf, rq); // cf = center frequency; rq = reciprocal quality
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)

// Iteration: 
//
// Here we are using `do` to iterate over a collection of four midi note numbers
// converted to cycles per second. This will pass each note into the synth
//
// This effectively plays a chord. 
(
[58,65,68,73].midicps.do{
  arg f;
  Synth(
    \bpfsaw,
    [
      \freq, f,
      \amp, 0.25,
    ]
  );

};
)

// If we add in randomness to the center frequency and reciprocal quality,
// we get some interesting harmonics. Here, the center frequency is between the
// first and the 12th partials and the reciprocoal quality is beteen 0.01 and
// 0.5
(
[58,65,68,73].midicps.do{
  arg f;
  Synth(
    \bpfsaw,
    [
      \freq, f,
      \amp, 0.25,
      // --- HERE'S THE NEW SHIT
      \cf, f * exprand(1,12), 
      \rq, exprand(0.01, 0.5)
    ]
  );

};
)

// ## Embracing randomness
//
// We could just create four synths and set random values for each every time. 
(
4.do{
  Synth(
    \bpfsaw,
    [
      \freq, exprand(100,1000),
      \amp, 0.25,
      // --- HERE'S THE NEW SHIT
      \cf, exprand(200,5000), 
      \rq, exprand(0.01, 0.5)
    ]
  );

};
)
// # Scales
//
// The Scale object is an abstract representation of a musical scale
//
// Scale.major
// Scale.minor
// Scale.directory // complete list
//
// Use this for frequency.
//
// the degrees method converts to integer scale degrees, which always starts at
// the low end of the keyboard. You can add 60 to convert to middle C
// 
// the midicps method always converts to cycles per second and choose will 
// select a random element from the scale array
Scale.minor.degrees //
(
4.do{
  Synth(
    \bpfsaw,
    [
      // --- SCALES!!!
      \freq, (Scale.minor.degrees + 60).midicps.choose,
      \amp, 0.25,
      \cf, exprand(200,5000), 
      \rq, exprand(0.01, 0.5)
    ]
  );

};
)

// ## Randomizing number of notes in chord
(
(1..6).choose.do{
  Synth(
    \bpfsaw,
    [
      // --- SCALES!!!
      \freq, (Scale.minor.degrees + 60).midicps.choose,
      \amp, 0.25,
      \cf, exprand(200,5000), 
      \rq, exprand(0.01, 0.5)
    ]
  );
};
)

// ## Notes currently stay constant, but we can use LFOs to modify this
(
  // This synthdef is a sawtwooth wave run through a bandpass filter
  // An envelope generator allows us to shape the sound over time. 
  // The LFO Noise controller will take muliplication parameter as "detune"
  // and apply it 0.5 x per second. 
  // Note that the output is in semitones, so to use it as multiplier, we need
  // to use the midiratio method
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), freq=500, detune=0.2, cf=1500, rq=0.2, amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- LFO here!
    sig = Saw.ar(freq * LFNoise1.kr(0.5,detune).midiratio); // once ever two seconds and scale it by the detune value
    sig = BPF.ar(sig, cf, rq); // cf = center frequency; rq = reciprocal quality
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)

Synth(\bpfsaw); // Not really noticable on its own
2.do{Synth(\bpfsaw, [\amp, 0.5])} // But indeed so if we use multiple
10.do{Synth(\bpfsaw, [\amp, 0.2])} // Hella instances are a chorus
10.do{Synth(\bpfsaw, [\amp, 0.2, \detune, 3])} // Larger detune creates meandering pitch-cluster effect

// ## Randomness in the bandpass filters can lead to some wacky results
// 
// Here, we are adjusting the center frequency and the reciprocal quality to
// vary along a specified range.
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), freq=500, detune=0.2, 
    cfmin=500, cfmax=2000, 
    rqmin=0.1, rqmax=0.2, 
    amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    sig = Saw.ar(freq * LFNoise1.kr(0.5,detune).midiratio); 
    sig = BPF.ar(
      sig,
      LFNoise1.kr(0.2).exprange(cfmin, cfmax),
      LFNoise1.kr(0.1).exprange(rqmin, rqmax),
    );
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)
(
10.do{
  Synth(
    \bpfsaw,
    [
      \freq, 50,
      \amp, 0.2,
    ],
  );
};
)
// Remember that we can multiply the base frequencies to get partials, so here
// we will let the center frequency be somehwere in the range of the 2nd and
// 50th partial, making it lower and increasing the strength of the LFO to give
// us strong sweeping harmonics
(
10.do{
  Synth(
    \bpfsaw,
    [
      \freq, 50,
      \amp, 0.2,
      \cfmin, 50*2,  // Lowering the center frequency pitch
      \cfmax, 50*50,
      \rfmin, 0.005, // Strengthening the LFOs
      \rfmax, 0.03,
    ],
  );
};
)
// ## Centering is as easy as pan
//
// Everything has been happening in the left channel. The easiest way to create
// a dual channel sound is to use the pan. Here we are adding a pan argument to
// control where that pan will lie.
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), 
    // --- PAN!
    freq=500, detune=0.2, pan=0,
    cfmin=500, cfmax=2000, 
    rqmin=0.1, rqmax=0.2, 
    amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    sig = Saw.ar(freq * LFNoise1.kr(0.5,detune).midiratio);
    sig = BPF.ar(
      sig,
      LFNoise1.kr(0.2).exprange(cfmin, cfmax),
      LFNoise1.kr(0.1).exprange(rqmin, rqmax),
    );
    sig = Pan2.ar(
      sig, 
      pan, // auto-random: LFNoise1.kr(0).rrand(-1, 1),
    );
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)
// With the argument, we can randomize the pan position for each synth!!!
(
10.do{
  Synth(
    \bpfsaw,
    [
      \freq, 50,
      \amp, 0.4,
      \cfmin, 50*2,  // Lowering the center frequency pitch
      \cfmax, 50*50,
      \rfmin, 0.005, // Strengthening the LFOs
      \rfmax, 0.03,
      \pan, rrand(-1.0, 1.0),
    ],
  );
};
)
// It's also possible to create the randomization using bang expansion.
//
// We can put the bang any place we want to, so we can do 
//
//     Out.ar(out, sig!2);
//
// or 
//
//     EnvGen.kr(...)!2;
//
// etc...
//
// Remember: you can duplicate processes using !2 or quadruple them using !4.
// That being said, remember that functions act differently in that the 
// evaluation happens after the duplication
//
// rrand(1, 10)!4; // [10, 10, 10, 10]
// {rrand(1, 10)}!4; // [10, 5, 2, 7]
//
//
// The example uses duplication of the LFOs. Adding the duplication without
// the function syntax means that they are evaluated then duplicated, but with
// the function syntax, we get a more random sound.
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), 
    // --- PAN!
    freq=500, detune=0.2, pan=0,
    cfmin=500, cfmax=2000, 
    rqmin=0.1, rqmax=0.2, 
    amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- Functional channel expansion with {LFO()}!2
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    sig = BPF.ar(
      sig,
      {LFNoise1.kr(0.2).exprange(cfmin, cfmax)}!2,
      {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    // --- BALANCE provides a way for us to get balance between left and right
    //     channels. 
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)
(
10.do{
  Synth(
    \bpfsaw,
    [
      \freq, 50,
      \amp, 0.4,
      \cfmin, 50*2,  // Lowering the center frequency pitch
      \cfmax, 50*50,
      \rfmin, 0.005, // Strengthening the LFOs
      \rfmax, 0.03,
      \pan, 0,
    ],
  );
};
)
// If you have a multi-speaker setup, PanAz() allows you to set up any number
// of speakers in a circle and do cool things with the sound such as sending it
// around the circle with an LFSaw controller or randomly with an LFNoise
// controller. 
//
// Registering a multispeaker setup requires you to specify the number of 
// hardware output channels and reboot the server:
//
// s.options.numOutputBusChannels_(8)
// s.reboot;
//
// I'm not going to go through the setup, but it's at 
// https://youtu.be/lGs7JOOVjag?t=795

// ## Nested Randomness
//
// randomness is key to making sound interesting, but nesting randomness can make
// things more interesting
// For example. A simple sine wave:
{SinOsc.ar(440, 0, 0.2!2)}.play
// Can randomly change pitch:
{SinOsc.ar(LFNoise0.kr(8).exprange(200,800), 0, 0.2!2)}.play
// ... but the rate at which the pitch changes is predictable... it's always 8x
// we can change this by adding another noise generator by replacing the 
// frequency with another noise generator
{SinOsc.ar(LFNoise0.kr(LFNoise0.kr(8).exprange(2,30)).exprange(200,800), 0, 0.2!2)}.play
// We can apply this to our running example by randomizing the frequency for the
// random frequency for the bandpass filter
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), 
    freq=500, detune=0.2, pan=0,
    cfmin=500, cfmax=2000, 
    cfhzmin=0.1, cfhzmax=0.3,
    rqmin=0.1, rqmax=0.2, 
    amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    sig = BPF.ar(
      sig,
      // --- MORE RANDOM!!!
      freq = {
        LFNoise1.kr(
          LFNoise1.kr(4).exprange(cfhzmin, cfhzmax)
        ).exprange(cfmin, cfmax)
      }!2,
      rq   = {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)
// By increasing the range of thes frequency, we increase the rate at which
// the envelope changes to create drastic effects
(
10.do{
  Synth(
    \bpfsaw,
    [
      \freq, 50,
      \amp, 0.5,
      \cfmin, 50*2, 
      \cfmax, 50*50,
      \rqmin, 0.01,
      \rqmax, 0.05,
      \cfhzmin, 1, 
      \cfhzmax, 16,
    ],
  );
};
)

// ## Low Shelf filter
//
// Last part: adding a low shelf filter. This allows you to boost or attenuate
// low frequencies.
(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), 
    freq=500, detune=0.2, pan=0,
    cfmin=500, cfmax=2000, 
    cfhzmin=0.1, cfhzmax=0.3,
    rqmin=0.1, rqmax=0.2, 
    lsf=200, ldb=0, // Setting this to 0 bypasses the filter
    amp=1, out=0;
    var sig, env;
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    sig = BPF.ar(
      sig,
      {
        LFNoise1.kr(
          LFNoise1.kr(4).exprange(cfhzmin, cfhzmax)
        ).exprange(cfmin, cfmax)
      }!2,
      {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    // --- LOW SHELF FILTER!!!
    sig = BLowShelf.ar(sig, lsf, 0.5, ldb);
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)

// Let's imagine we want to create chord progression. We use Pbind to create
// the progression with Pseq creating the chords. Here's an example with simple
// notes:
(
Pbind(
  \instrument, \bpfsaw,
  \dur, 1, // duration
  \midinote, Pseq([54,61,56], 1), // notes to play
  // Synth options
  \detune, 0.08,
  \cfmin, 100,
  \cfmax, 1500,
  \atk, 1,
  \ldb, 6,
  \amp, 0.2,
  \out, 0, 
).play
)
// To create chords, we stack arrays within the progression array. This creates
// a predictable pattern of chords. 
(
Pbind(
  \instrument, \bpfsaw,
  \dur, 5, // duration
  \midinote, Pseq([
    [23,25,54,63,64],
    [45,52,54,59,61,64],
    [28,40,47,56,59,63],
  ], 1), 
  // Synth options
  \detune, 0.08,
  \cfmin, 100,
  \cfmax, 1500,
  \atk, 2,
  \rel, 8,
  \ldb, 6,
  \amp, 0.2,
  \out, 0, 
).play
)
// If we want to spice things up, we can use Pxrand([], inf) to randomly choose
// chords from the array forever. Because it's going on forever, we would want
// to assign it to a variable so that we can stop it.
(
~chords = Pbind(
  \instrument, \bpfsaw,
  \dur, Pwhite(4.5, 7.0, inf), // duration
  \midinote, Pxrand([ // Note: changed to pxrand
    [23,25,54,63,64],
    [45,52,54,59,61,64],
    [28,40,47,56,59,63],
    [42,52,57,61,63], // added pattern
  ], inf),            // goes on forever
  // Synth options
  \detune, Pexprand(0.05, 0.2, inf),
  \cfmin, 100,
  \cfmax, 1500,
  \rqmin, Pexprand(0.01, 0.15, inf),
  \akt, Pwhite(2.0, 2.5, inf),
  \rel, Pwhite(6.5, 10.0, inf),
  \ldb, 6,
  \amp, 0.6,
  \out, 0,
).play;
)
~chords.stop;

// # Stretching the  Synths
//
// We can use the synthdef to create an entirely different sound by modifying
// argument values. If we lower the pitch of the sawtooth wave, then it gets a
// more percussive quality below 20Hz
//
// We can use the BP filter to draw out certain qualities of the sounds.
(
Synth.new(
  \bpfsaw,
  [
    \freq, 2,
    \amp, 1.5,
    \atk, 0,
    \rqmin, 0.005,
    \rqmax, 0.008,
  ],
);
)
// Because the center frequency varies within the synth via a range, we can set
// this range to be equal to fix the frequency of this sound.
(
Synth.new(
  \bpfsaw,
  [
    \freq, 2,
    \amp, 1.5,
    \atk, 0,
    \rqmin, 0.005,
    \rqmax, 0.008,
    \cfmin, 880, // fixing the center frequency to be 880
    \cfmax, 880,
  ],
);
)
// Now that we have this in our toolbox, we can use the randomization tools to
// randomize the duration and frequency of the strikes. This example sounds a
// bit like a drunken walk.
(
~marimba = Pbind(
  \instrument, \bpfsaw,
  \dur, Pexprand(0.1, 1, inf), // random duration for the strikes
  \freq, Pexprand(0.25, 9, inf), // random tempos
  \detune, 0,
  \rqmin, 0.005,
  \rqmax, 0.008,
  \cfmin, 150,  // wide range of frequencies
  \cfmax, 1500,
  \amp, 1.5,
  \out, 0,
).play;
)

~marimba.stop;
