# Learning Supercollider

I'm currently using supercollider version 3.9.3 and trying to figure out how I
can use this to make music.

# Tutorials I'm Using

### [Eli Fieldsteel's SuperCollider Video Tutorials](https://www.youtube.com/playlist?list=PLPYzvS8A_rTaNDweXe6PX4CXSGq4iEWYC)

As of this moment, I'm up to lesson 4 of this video series, but it is much
better than attempting to learn from reading through the manual. 

# My setup

I'm using SuperCollider version 3.10.0 on macOS and Ubuntu 18.04.

## Ubuntu

On Ubuntu, I'm using vim 8 and the
[supercollider/scvim](https://github.com/supercollider/scvim/) plugin. Because
the F\# keys require me to press the <kbd>Fn</kbd> key at the same time, I've
[remapped these in my `.vimrc`](https://github.com/zkamvar/config-files/commit/4167c060c61201283cee841ddd511c359001ad19):

```vim
" Remap the F5 and F6 to ones that work better for my keyboard since it's kind
" of awkward for me to type both fn and F{5,6} keys at the same time. I'm
" copying these directly from 
" https://github.com/supercollider/scvim/blob/master/plugin/supercollider.vim

au Filetype supercollider nnoremap <leader>sc :call SClangStart()<CR>
au Filetype supercollider nnoremap <leader>b :call SendToSC('s.boot;')<CR>
au Filetype supercollider nnoremap <leader>t :call SendToSC('s.plotTree;')<CR>
au Filetype supercollider nnoremap <leader>m :call SendToSC('s.meter;')<CR>

" Sending a block of sc code <leader>f
au Filetype supercollider nnoremap <leader>f :call SClang_block()<CR>
au Filetype supercollider inoremap <leader>f :call SClang_block()<CR>a
au Filetype supercollider vnoremap <leader>f :call SClang_send()<CR>

" Sending a single line is <Space>
au Filetype supercollider vnoremap <buffer> <Space> :call SClang_line()<CR>
au Filetype supercollider nnoremap <buffer> <Space> :call SClang_line()<CR>
au Filetype supercollider inoremap <buffer> <Space> :call SClang_line()<CR>a

" Hardstop is <leader>x
au Filetype supercollider nnoremap <leader>x :call SClangHardstop()<CR>
```

### Installation pains

I had installed SuperCollider from source using the [instructions for
Linux](https://github.com/supercollider/supercollider/blob/master/README_LINUX.md)
help file. One of the major pain points was attempting to keep track of the 
ever-changing constellation of dependencies. At the time, the dependency list
looked like this for the sound system:

```
sudo apt-get install build-essential cmake libjack-jackd2-dev libsndfile1-dev libfftw3-dev libxt-dev libavahi-client-dev
```

And this for Qt:

```
sudo apt-get install qt5-default qt5-qmake qttools5-dev qttools5-dev-tools qtdeclarative5-dev qtwebengine5-dev libqt5svg5-dev
```

 - First pain point: Jack2. I was getting a weird error:
      exec of JACK server (command = "/usr/bin/jackd") failed: No such file or directory
   This turned out to be solved by using `sudo apt install jackd2`.
 - Second paint point: 
      Could not find a package configuration file provided by "Qt5WebSockets"
   I [found the solution on an ubuntu stack exchange site](https://askubuntu.com/questions/374755/what-package-do-i-need-to-build-a-qt-5-cmake-application)
   that suggested to use the [apt-file](https://wiki.ubuntu.com/AptFile) 
   utility. I used it to find that the right package to install was 
   `libqt5websockets5-dev`  
 - Third pain point: I can only run this as root. I still don't know how to
   address this problem but if I attempt to run `sclang` as a normal user, I get
   a warning that it couldn't set realtime scheduling priority. When I run 
   `s.boot;`, I'm met with an error: `Exception in World_New: Permission Denied`.
   This one really is only a minor inconvenience, but I feel a bit uneasy when I
   must run SC as root

## MacOS 

For MacOS, I'm using the scide because there is additional pain in getting scvim
to work properly with iTerm2.
