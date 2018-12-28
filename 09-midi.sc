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
MIDIdef.noteOn(\noteOnTest, {
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
MIDIdef.noteOn(\noteOnTest, {
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

// Using a sustaining MIDI while dealing with MIDI polyphony
(
SynthDef.new(\polytone, {
    arg freq = 440, amp = 0.3, gate = 0;
    var sig, env;
    sig = LFTri.ar(freq)!2;
    // Make sure the sin waves turn themselves off somehow
    env = EnvGen.kr(Env.adsr, gate: gate doneAction: 2); // short bursts
    // velocity used in the traditional sense
    // We are mapping the linear range [1, 127] to
    // the exponential range [0.01, 0.3]
    sig = sig * env * amp;
    Out.ar(bus: 0, channelsArray: sig);
}).add;
)

// One way to deal with the polyphony is to create an array of length 128,
// one element for each midi note.
(
~notes = Array.newClear(128);

MIDIdef.noteOn(\noteOnTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    ~notes[nn] = SynthDef.new(
        \polytone,
        [
            \freq, nn.midicps,
            \amp, vel.linexp(1, 127, 0.01, 0.3)
        ]
    );
});

// But! This only handles note On messages and if we play them, then
// they will be stuck indefinitely. To handle this, we need to create
// a note off midi def.

MIDIdef.noteOff(\noteOffTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    ~notes[nn].set(\gate, 0);
    ~notes[nn] = nil;
});
)

// MIDI def with pitch bend. Note that in the video, Eli noticed there were
// three channels being used by the wheel. He adjusted the midi def to use
// only the the zero channel


(

~bend = 8192; // global variable to track pitch wheel position;
MIDIdef.bend(\bendTest, {
    arg vel, chan, src;
    [vel, chan, src].postln;
    ~bend = val; // update global variable to keep track
    // iterate over all the notes and transpose from one linear scale
    // to another.
    ~notes.do{arg synth; synth.set(\bend, val.linlin(0, 16383, -2, 2))};
}, chan: 0)



SynthDef.new(\polybend, {
    arg freq = 440, amp = 0.3, gate = 0, bend = 0;
    var sig, env;
    sig = LFTri.ar(freq * bend.midiratio)!2; // midiratio: convert from semitones to a ratio
    // Make sure the sin waves turn themselves off somehow
    env = EnvGen.kr(Env.adsr, gate: gate doneAction: 2); // short bursts
    // velocity used in the traditional sense
    // We are mapping the linear range [1, 127] to
    // the exponential range [0.01, 0.3]
    sig = sig * env * amp;
    Out.ar(bus: 0, channelsArray: sig);
}).add;

MIDIdef.noteOn(\noteOnTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    ~notes[nn] = SynthDef.new(
        \polybend,
        [
            \freq, nn.midicps,
            \amp, vel.linexp(1, 127, 0.01, 0.3)
        ]
    );
});

// But! This only handles note On messages and if we play them, then
// they will be stuck indefinitely. To handle this, we need to create
// a note off midi def.

MIDIdef.noteOff(\noteOffTest, {
    // val,vel = message value (e.g velocity, control value, etc)
    // num,nn = note number
    // chan = MIDI channel <- not really used
    // src = MIDI source   <- not really used
    arg vel, nn, chan, src;
    [vel, nn, chan, src].postln;
    ~notes[nn].set(\gate, 0);
    ~notes[nn] = nil;
});

)
