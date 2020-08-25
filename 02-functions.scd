s.boot;
// The function is fairly simple. It needs to take in args and use them.
// The args are specified at the top of the function and can be used.
// Any which way. Here is a random tone generator from the help video:
// Modifying the argument 'hz' will change how fast the tones jump around.
(
z = {
    arg hz = 8;
    var freq, sig;
    freq = LFNoise0.kr(hz).exprange(200, 1000);
    sig = SinOsc.ar(freq);
}.play;
)
z.set(\hz, 2);
z.set(\hz, pi * 3);
z.free;

// All of the global variables must be a single letter (except s) OR
// start with a tilde. Here I'm using a Sin oscillator to control a
// saw.
(
~z_saw = {
    arg hz = 0.5;
    var freq, sig;
    freq = SinOsc.kr(hz).exprange(10, 100);
    sig = Saw.ar(freq);
}.play;
)

~z_saw.set(\hz, 1);
~z_saw.free;
s.quit;