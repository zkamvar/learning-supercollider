s.boot;
(
SynthDef.new(\pulseTest, {
	// Arguments:
	// @param ampHz the amplitude in Hz
	// @param fund the fundamental starting frequency for the tones
	// @param maxPartial the maximum limit for partial overtones
	// @param width the width of the pulse
	arg ampHz=4, fund=40, maxPartial=4, width=0.5;
	var amp1, amp2, freq1, freq2, sig1, sig2;
	// Generate varaible amplitude
	amp1 = LFPulse.kr(ampHz, 0, 0.12) * 0.75;
	amp2 = LFPulse.kr(ampHz, 0.5, 0.12) * 0.75;
	// Generate tones
	freq1 = LFNoise0.kr(4).exprange(fund, fund*maxPartial).round(fund);
	freq2 = LFNoise0.kr(4).exprange(fund, fund*maxPartial).round(fund);
	freq1 = freq1 * LFPulse.kr(8, add:1);
	freq2 = freq2 * LFPulse.kr(6, add:1);
	// Create square wave signlas with the above tones and amps
	sig1  = Pulse.ar(freq1, width, amp1);
	sig2  = Pulse.ar(freq2, width, amp2);
	// Adddddddd Reeeeverrrrrbbbbbb
	sig1  = FreeVerb.ar(sig1, 0.7, 0.8, 0.25);
	sig2  = FreeVerb.ar(sig2, 0.7, 0.8, 0.25);
	Out.ar(0, sig1);
	Out.ar(1, sig2);
}).add;
)

x = Synth.new(\pulseTest);

x.set(\width, 5.5);
x.set(\fund, 80);
x.set(\maxPartial, 9);
x.set(\ampHz, 4);
x.free;

x = Synth.new(\pulseTest, [\ampHz, 3.3, \fund, 48, \maxPartial, 4, \width, 0.15]);
x.free;
s.quit;
