// Take a look at the guide Using MIDI

MIDIClient.init; // allows SC to connect with midi functionality of your OS
// This will return in the post window the following:
// MIDI Sources:
// MIDI Destinations:
//
// This will not show anything if there is no midi keyboard to use.

// Next step is to connect the midi device to SC.
MIDIIn.connectAll;

// To add MIDI data, use MIDIfunc and MIDIdef
//
// Note that midi information takes Note ON and Note OFF messages
MIDIdef.noteOn(\noteOnTest, {"key down".postln});

// Temporarily disable MIDIdef with .disable()
MIDIdef(\noteOnTest).disable();
// Reactivate with enable
MIDIdef(\noteOnTest).enable();
// Destroy a MIDIdef
MIDIdef(\noteOnTest).free;
// Destroy ALL MIDIdefs
MIDIdef.freeAll;

// By default, MIDIdefs are also destroyed with cmd . This can be
// prevented with permanent_(true);
MIDIdef.noteOn(\noteOnTest, {"key down".postln}).permanent_(true);

// Printing MIDI data
(
MIDIdef(\noteOnTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    {
        var sig, env;
        sig = LFTri.ar(nn.midicps)!2;
        // Make sure the sin waves turn themselves off somehow
        env = EnvGen.kr(Env.perc, doneAction: 2); // short bursts
        // velocity used in the traditional sense
        // We are mapping the linear range [1, 127] to
        // the exponential range [0.01, 0.3]
        sig = sig * env * vel.linexp(1, 127, 0.01, 0.3);
    }.play;
});
)
// Start server
s.boot;

// play some midi notes


// This approach is quick and dirty, but it doesn't incorporate things like
// note off or pitch bends. The most flexible way of creating sound is to
// create a synthdef
(
SynthDef.new(\tone, {
    arg freq = 440, amp = 0.3;
    var sig, env;
    sig = LFTri.ar(freq)!2;
    // Make sure the sin waves turn themselves off somehow
    env = EnvGen.kr(Env.perc, doneAction: 2); // short bursts
    // velocity used in the traditional sense
    // We are mapping the linear range [1, 127] to
    // the exponential range [0.01, 0.3]
    sig = sig * env * amp;
    Out.ar(bus: 0, channelsArray: sig);
}).add;
)

// Test to see that this works
SynthDef.new(\tone, [\freq, 700, \amp, 0.5]);

// Now instead of providing a function to the MIDIdef, we can use a
// SynthDef and add the correct parameters
(
MIDIdef(\noteOnTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    SynthDef.new(
        \tone,
        [
            \freq, nn.midicps,
            \amp, vel.linexp(1, 127, 0.01, 0.3)
        ]
    );
});
)