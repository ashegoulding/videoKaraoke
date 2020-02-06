/*
 * This file is part of VideoKaraoke.
 *
 * VideoKaraoke is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VideoKaraoke is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VideoKaraoke.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2020, LEE Jae-Sung.
 */

package com.jae_sung.videokaraoke;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JFrame;

class Controller implements KeyListener, FocusListener {
	Playlist playlist;
	TopPanel topPanel;
	MenuPanel menuPanel;
	VlcPanel vlc;
	
	public Controller() {
		int nTopR = 0, nTopG = 0, nTopB = 0;
		String strKaraokeName = "노래방", strIntroVideo = "./intro.mp4";
		try {
			Properties p = new Properties();
			System.out.println();
			p.load(new FileInputStream("./setting.ini"));
			
			nTopR = Integer.parseInt(p.getProperty("TopBK_R"));
			nTopG = Integer.parseInt(p.getProperty("TopBK_G"));
			nTopB = Integer.parseInt(p.getProperty("TopBK_B"));
			strKaraokeName = new String(p.getProperty("KaraokeName").getBytes("ISO-8859-1"), "UTF-8");
			strIntroVideo = p.getProperty("introVideo");
		}
		catch(Exception e) { e.printStackTrace(); }
		
		playlist = new Playlist("./playlist.txt");
		System.out.println(playlist);
		JFrame f = new JFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		topPanel = new TopPanel(nTopR, nTopG, nTopB, strKaraokeName);
	    topPanel.setBounds(0, 0, screenSize.width, screenSize.height / 15);
	    menuPanel = new MenuPanel(50, 50, 50, f);
	    menuPanel.setBounds(screenSize.width / 10, screenSize.height/10, (int)(screenSize.width * 0.8), (int)(screenSize.height * 0.8));
	    menuPanel.setVisible(false);
	    f.add(topPanel);
	    f.add(menuPanel);
		vlc = new VlcPanel(topPanel, strIntroVideo);
		f.add(vlc);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setUndecorated(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.addKeyListener(this);
		f.addFocusListener(this);
		vlc.startVLC();
	}
	
	private void playSound(String FilePath) {
		try {          
			File soundFile = new File(FilePath);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile); 
			AudioFormat format = audioIn.getFormat();             
			
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			Clip clip = (Clip)AudioSystem.getLine(info);
			
			clip.open(audioIn);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int nKeyCode = e.getKeyCode();
		switch(nKeyCode) {
		// 시작
		case KeyEvent.VK_ENTER:
			if(vlc.addFirstSong(playlist.searchSong(topPanel.getInputNumber())))
				vlc.playSong();
			else if(topPanel.getInputNumber() != -1)
				topPanel.setTempMsg("안내 | 없는 곡입니다.");
			else if(vlc.getSongCount() > 0)
				vlc.playSong();
			topPanel.inputNumber((char)KeyEvent.VK_CLEAR);
			break;
		// 취소
		case KeyEvent.VK_ESCAPE:
			if(menuPanel.isVisible()) {
				menuPanel.init();
				menuPanel.setVisible(false);
			}
			else {
				vlc.cancelSong();
			}
			break;
		// 일시정지
		case KeyEvent.VK_SPACE:
			vlc.pauseSong();
			break;
		// 예약
		case KeyEvent.VK_CONTROL:
			if(vlc.addSong(playlist.searchSong(topPanel.getInputNumber())))
				topPanel.setTempMsg(topPanel.getInputNumber() + " | 예약되었습니다.");
			topPanel.inputNumber((char)KeyEvent.VK_CLEAR);
			break;
		// 우선예약
		case KeyEvent.VK_SHIFT:
			if(vlc.addFirstSong(playlist.searchSong(topPanel.getInputNumber())))
				topPanel.setTempMsg(topPanel.getInputNumber() + " | 우선예약되었습니다.");
			topPanel.inputNumber((char)KeyEvent.VK_CLEAR);
			break;
		// 방향키 이동
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
			if(menuPanel.isVisible())
				menuPanel.arrowKeyInput(nKeyCode);
			break;
		// 메뉴
		case KeyEvent.VK_F1:
			if(menuPanel.isVisible())
				menuPanel.init();
			menuPanel.setVisible(!menuPanel.isVisible());
			break;
		// 반주 작게
		case KeyEvent.VK_F2:
			vlc.volumeDown();
			break;
		// 반주 크게
		case KeyEvent.VK_F3:
			vlc.volumeUp();
			break;
		// 템포 느리게
		case KeyEvent.VK_F4:
			vlc.tempoDown();
			break;
		// 템포 빠르게
		case KeyEvent.VK_F5:
			vlc.tempoUp();
			break;
		// 박수
		case KeyEvent.VK_F6:
			playSound("./res/clap.wav");
			break;
		// 선택
		case KeyEvent.VK_F7:
			if(menuPanel.isVisible())
				menuPanel.selectInput();
			break;
		case KeyEvent.VK_0:
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
		case KeyEvent.VK_5:
		case KeyEvent.VK_6:
		case KeyEvent.VK_7:
		case KeyEvent.VK_8:
		case KeyEvent.VK_9:
		case KeyEvent.VK_BACK_SPACE:
			topPanel.inputNumber((char)nKeyCode);
			break;
		case KeyEvent.VK_S:
			if(e.isControlDown()) {
				playlist.exportHTML();
				break;
			}
		default:
			System.out.println(nKeyCode);
			break;
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void focusLost(FocusEvent e) {
		if(menuPanel.getSelectedPanel() != menuPanel.PANEL_SEARCH)
			((JFrame)e.getSource()).requestFocus();
	}
}

public class VideoKaraoke {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length > 0) {
			if(args[0].equals("-ver"))
				System.out.println("VideoKaraoke version 0.0.3");
			else if(args[0].equals("-playlist"))
				System.out.println(new Playlist("./playlist.txt"));
			else if(args[0].equals("-artist"))
				if(args.length > 1)
					System.out.println(new Playlist("./playlist.txt").toArtistString(args[1]));
				else
					System.out.println(new Playlist("./playlist.txt").toArtistString(null));
			else
				System.out.println("Unrecognized option: " + args[0]);
			return;
		}
		else {
			new Controller();
		}
	}
}
