(
  SynthDef(\bpfsaw, {
    arg atk=2, sus=0, rel=3, c1=1, c2=(-1), // Envelope arguments
    freq=500,    detune=0.2,  // Waveform arguments
    cfmin=500,   cfmax=2000,  // bandpass filter argumetns
    cfhzmin=0.1, cfhzmax=0.3, // ...
    rqmin=0.1,   rqmax=0.2,   // ...
    lsf=200,     ldb=0,       // Low shelf filter arguments
    pan=0,                    // *hook voice* PAN?
    amp=1,       out=0;       // amplitude and output channel
    var sig, env;
    // --- envelope generator (attack, sustain, release)
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- Oscillator (generates the signal) /\/\/\/\/
    sig = Saw.ar(freq * {LFNoise1.kr(0.5,detune).midiratio}!2);
    // --- bandpass filter
    sig = BPF.ar(
      sig,
      // center frequency of the bandpass filter (what gets through)
      {
        // Generates random center frequencies in [cfmin, cfmax] ...
        LFNoise1.kr(
          // ... at a rate between cfhzmin and cfhzmax.
          LFNoise1.kr(4).exprange(cfhzmin, cfhzmax)
        ).exprange(cfmin, cfmax)
      }!2,
      // reciprocal quality: lower is a stronger BP effect
      // Generates random quality value in [rqmin, rqmax]
      {LFNoise1.kr(0.1).exprange(rqmin, rqmax)}!2,
    );
    // --- low shelf filter
    sig = BLowShelf.ar(sig, lsf, 0.5, ldb);
    // --- channel balancer
    sig = Balance2.ar(sig[0], sig[1], pan);
    sig = sig * env * amp;
    Out.ar(out, sig);
  }).add;
)

(
~marimba = Pbind(
  \instrument, \bpfsaw,
  \dur, Prand([1, 0.5], inf), // A new synth every second or half second
  \freq, Prand([ // related beat signatures from 1 every two seconds to 8/s
    1/2, 2/3, 1, 4/3, //2, 5/2, 3, 4, 6, 8 
  ], inf),
  \detune, 0,
  \rqmin, 0.005,
  \rqmax, 0.008,
  \cfmin, Prand((Scale.major.degrees+64).midicps, inf) * Prand([0.5, 1, 2, 4], inf),
  \cfmax, Pkey(\cfmin) * Pwhite(1.008, 1.025, inf),
  \atk, 3, 
  \sus, 1,
  \rel, 5,
  \amp, Pexprand(0.9, 1.1, inf),
  \out, 0,
).play;

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
  \amp, Pexprand(0.3, 0.4, inf),
  \out, 0,
).play;
)
~chords.stop;
~marimba.stop;
