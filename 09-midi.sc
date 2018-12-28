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
});
)