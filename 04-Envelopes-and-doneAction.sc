// Many times we want a synth to fade in and fade out.
// This can be accomplished with Envelope Ugens
s.boot;
s.plotTree; // Visualise the active synths
(
// Here, we create and play an anonymous synth.
// The doneAction: 2 indicates that SC should free the synth
// Once it is finished.
{
    var sig, freq, env;
    // Using XLine is an expoential version. This gives a more
    // "Natural" sound. We would use this UNLESS we are specifying
    // Amplitude in decibles. If we use decibles (a linear scale),
    // Then we would convert with the dbamp method
    //
    // If there are two envelopes, then the one with the shorter duration will
    // execute its doneAction first, so here we set the doneAction to 0
    env = XLine.kr(start: 1, end: 0.01, dur: 3, doneAction: 0);
    // Frequency is also preceived on an exponential scale.
    freq = XLine.kr(start: 880, end: 110, dur: 5, doneAction: 2);
    sig = Pulse.ar(freq) * env; //env.dbamp; // convert from db to amp
}.play;
)


s.quit;