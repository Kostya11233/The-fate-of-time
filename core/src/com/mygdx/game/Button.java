package com.mygdx.game;
//https://gl.serenanet.one/sub/7ChGUFfwBcrSqJtoFESKgehGt?providerid=ZOth3lct
//https://www.dropbox.com/scl/fi/94o3hfv6qpxaucdk4jhzc/IMG_20260512_191321_204.jpg?rlkey=oi8bdfhkg53pqxdevi1523dwy&st=ey8166le&dl=0
//https://www.dropbox.com/scl/fi/8tlazz9gusz834ddwvxqn/IMG_20260512_184238_812.jpg?rlkey=zosjgj98f81sspyx7lw2j03nd&st=ywvbazhb&dl=0
//https://www.dropbox.com/scl/fi/b06rpq17uau7nzv5gbr81/IMG_20260512_181524.jpg?rlkey=od2p362m01m6uvyg42sh1fent&st=9dtiliwh&dl=0
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button {
    BitmapFont font;
    Texture texture;
    String text;
    int x, y;
    int textX, textY;
    int buttonWidth, buttonHeight;
    int textWidth, textHeight;
    public Button(int x, int y, int buttonWidth, int buttonHeight, String text) {
        this.x = x;
        this.y = y;
        //https://www.dropbox.com/scl/fi/s06rg6titvamtzwjnz14b/music.zip?rlkey=utbbxd2vvgjl02hn55irewnv6&st=tn2x7dhc&dl=0
        font = new BitmapFont();
        font.getData().scale(1f);
        font.setColor(Color.GOLD);
        textWidth = 200;
        textHeight = 67;
        texture = new Texture("badlogic.jpg");
        textX = x + (buttonWidth - textWidth) / 2;
        textY = y + (buttonHeight + textHeight) / 2;
        this.buttonWidth = 267;
        this.buttonHeight = 120;
        this.text = text;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, buttonWidth, buttonHeight);
        font.draw(batch, text, textX, textY);
    }

    public void dispose() {
        texture.dispose();
        font.dispose();
    }

    public boolean isHit(int tx, int ty) {
        return tx >= x && tx <= x + buttonWidth && ty >= y && ty <= y + buttonHeight;
    }
}
