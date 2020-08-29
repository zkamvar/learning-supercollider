(
SynthDef(\bpfbuf, {
    arg atk=0, sus=0, rel=3, c1=1, c2=(-1),
        buf=0, rate=1, spos=0,
        freq=440, rq=1, bpfmix=0,
        pan=0, amp=1, out=0;
    var sig, env;
    // --- Flat envelope generator with controls for curves of atk and sustain
    env = EnvGen.kr(Env([0,1,1,0], [atk,sus,rel], [c1,0,c2]), doneAction: 2);
    // --- All of our sound files are MONO, so the first arg is 1
    //     Note that the ir method of BufRateScale is more efficient because we
    //     know that we will not be chaning the bufnum while the synth is running.
    sig = PlayBuf.ar(2, buf, rate: rate * BufRateScale.ir(buf), startPos: spos);
    // --- Two-channel cross-fade between the raw signal and the BPF signal.
    //     For the BPF, we are giving the reciprocal between quality and
    //     amplitude so that we don't sacrifice loudness for quality.
    sig = XFade2.ar(sig, BPF.ar(sig, freq, rq, mul: 1/rq.sqrt), pan: bpfmix*2-1);
    sig = sig * env;
    sig = Pan2.ar(sig, pan, level: amp);
    Out.ar(out, sig);
  }).add;

// What IS reverb?
//
// it's the result of sound waves propgating in an enclosed space. It's the sum
// of many different feedback delays. It's a balance between dry and wet.
SynthDef(\reverb, {
    arg in, predelay=0.1, revtime=1.8, lpf=4500, mix=0.15, amp=1, out=0;
    var dry, wet, temp, sig; // temp is [temp]orary
    dry  = In.ar(in, 2);
    temp = In.ar(in, 2);
    wet = 0;
    // DelayN creates the room size with No interpolation
    temp = DelayN.ar(temp, maxdelaytime: 0.2, delaytime: 2, mul: predelay);
    // Create the delayed signals. Since we are dealing with fixed delay times,
    // we do not need or want interpolation here.
    16.do{
        temp = AllpassN.ar(temp, 0.05, {Rand(0.001, 0.05)}!2, revtime);
        // In a lot of real world cases, high frequencies get absorbed more quickly
        // than lower frequencies. Dampening like this can be acheived with a
        // Lowpass filter
        temp = LPF.ar(temp, freq: lpf);
        // add temporary delay line to the wet signal.
        wet = wet + temp;
    };
    sig = XFade2.ar(dry, wet, mix*2-1, amp);
    Out.ar(out, sig);
}).add;
)