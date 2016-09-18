(play{a=LFNoise0.kr(12).exprange(110,880);Resonz.ar(LPF.ar(CombN.ar(SinOsc.ar(a!2,0,0.2),2,0.2,10),48,16),880,0.5,8)};)

play{CombN.ar(Blip.ar(Duty.ar(1/[9,8],0,Dseq(\AVVVF.ascii.midicps,inf)/a=2+Blip.ar(3/[8,9],2).round),c=a**a.lag,c+5)).tanh};

play{b=Blip;c=TChoose.kr(Impulse.kr(2),[2,4,20,40,60,80,140,260]*8);Limiter.ar(sin(c*b.ar((c/400)+b.ar(c,4),c/24)),0.5)!2};

play{a=SinOscFB;LocalOut kr:d=a.ar(Duty.kr(LocalIn kr:8,a.kr(1/16,1),Dseq((1..8)*50,inf)),a.kr(1/(1..8))+1/2);Splay ar:d/2}//