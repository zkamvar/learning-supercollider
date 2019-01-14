// http://sccode.org/1-5bd

(
SynthDef(\fm7_timpani,
{
    arg  note = 60,out_bus=0,amp=0.5,sustain = 2,release = 0.5,cutoff = 10000,rq = 1,pan = 0,lfo_speed = 5,lfo_amp = 0;
	var ctls, mods, chans,out,pitch,gate,

        envAmp1,envAmp2,envAmp3,envAmp4,envAmp5,envAmp6,env1,
		envEnv1,envEnv2,envEnv3,envEnv4,envEnv5,envEnv6,envAmp,envEnv,lfo;

        gate = Trig1.kr(1, sustain + release);

		envEnv1 = Env.new([0 ,1.0 ,0 ,0 ,0], [0.0008 ,sustain ,0.0 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp1 = EnvGen.kr(envEnv1,gate,doneAction: 0);

		envEnv2 = Env.new([0 ,0.3535 ,0 ,0 ,0], [0.0005 ,0.0829 ,0.0 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp2 = EnvGen.kr(envEnv2,gate,doneAction: 0);
        envEnv3 = Env.new([0 ,0.2985 ,0.029 ,0 ,0], [0.0005 ,0.0136 ,sustain ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		//envEnv3 = Env.new([0 ,0.2985 ,0.029 ,0 ,0], [0.0005 ,0.0136 ,12.8286 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp3 = EnvGen.kr(envEnv3,gate,doneAction: 0);
        envEnv4 = Env.new([0 ,0.6332 ,0.0797 ,0 ,0], [0.0006 ,2.057 ,sustain ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		//envEnv4 = Env.new([0 ,0.6332 ,0.0797 ,0 ,0], [0.0006 ,2.057 ,37.2485 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp4 = EnvGen.kr(envEnv4,gate,doneAction: 0);

		envEnv5 = Env.new([0.0 ,0.1151 ,0.0 ,0.0 ,0.0], [0.0003 ,1.0517 ,0.0 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp5 = EnvGen.kr(envEnv5,gate,doneAction: 0);

		envEnv6 = Env.new([0.0 ,0.1055 ,0.0 ,0.0 ,0.0], [0.0004 ,215.6751 ,0.0 ,0.0], [3.0 ,-3.0 ,-3.0 ,-3.0],3);
		envAmp6 = EnvGen.kr(envEnv6,gate,doneAction: 0);

        envEnv = Env.new([0,1,1,0], [0.0001,sustain,release],[0,0,0]);
		envAmp = EnvGen.kr(envEnv,gate,doneAction:2);

		lfo = LFTri.kr(lfo_speed);
		lfo = LinLin.ar(lfo, 0, 1, lfo_amp.neg.midiratio  , lfo_amp.midiratio);


        pitch = note.midicps;

   ctls = [
		    [0.5 * 1 * (pitch  + ((7-7)/32))* lfo, 0, envAmp1],

		    [0.5 * 1 * (pitch  + ((10-7)/32))* lfo , 0, envAmp2],

		    [0.5 * 1.36 * (pitch  + ((4-7)/32))* lfo , 0, envAmp3],

		    [0.5 * 1.75 * (pitch  + ((7-7)/32))* lfo , 0, envAmp4],

		    [0.5 * 1 * (pitch  + ((7-7)/32))* lfo , 0, envAmp5],

		    [0.5 * 1.56 * (pitch  + ((7-7)/32))* lfo, 0, envAmp6]
	];

	mods = [
		[0, 12.5664, 12.5664, 0, 12.5664, 0],
		[0, 0, 0, 0, 0, 0],
		[0, 0, 0, 12.5664, 0, 0],
		[0, 0, 0, 0, 0, 0],
		[0, 0, 0, 0, 0, 12.5664],
		[0, 0, 0, 0, 0, 5.5418]
	];
	chans = [0, 1, 2, 3, 4, 5];
	out = FM7.ar(ctls, mods).slice(chans) * -12.dbamp;
	out = Mix.new([
		(out[0] * 1),
		(out[1] * 0),
		(out[2] * 0),
		(out[3] * 0),
		(out[4] * 0),
		(out[5] * 0),
	]);
	out = RLPF.ar(out,cutoff,rq);
	Out.ar(out_bus,Pan2.ar(out,pan,amp));
}).add;
)

Synth(\fm7_timpani);
Synth(\fm7_timpani,[\sustain,5,\release,0.1,\amp,0.5,\lfo_speed,5,\lfo_amp,0.05]);

(
b = Pbind(
	\instrument,\fm7_timpani,
	\note, Pseq([48, 55, 60, 67, 72, 79], inf),
	\sustain,2,
	\release,0.5,
    \pan,Pwhite(-1.0,1.0,inf),
	\lfo_amp,0,
	\dur, 0.5,
	\amp, 0.5
).play;
)

b.stop;