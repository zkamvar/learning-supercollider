// Many times we want a synth to fade in and fade out.
// This can be accomplished with Envelope Ugens
s.boot;
s.plotTree; // Visualise the active synths
(
// Here, we create and play an anonymous synth.
// The doneAction: 2 indicates that SC should free the synth
// Once it is finished.
{
    var sig, env;
    // Using XLine is an expoential version. This gives a more
    // "Natural" sound. We would use this UNLESS we are specifying
    // Amplitude in decibles. If we use decibles (a linear scale),
    // Then we would convert with the dbamp method
    env = XLine.kr(start: 1, end: 0.01, dur: 0.2, doneAction: 2); // units: amp
    sig = Pulse.ar(ExpRand(30, 500)) * env; //env.dbamp; // convert from db to amp
}.play;
)


s.quit;