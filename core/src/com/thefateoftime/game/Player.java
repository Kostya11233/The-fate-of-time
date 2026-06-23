package com.thefateoftime.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private Body body;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private TextureRegion standRight, standLeft;
    private float stateTime;
    private boolean facingRight = true;
    private boolean movingLeft = false, movingRight = false;
    private boolean isGrounded = false;
    private float speed = 5f;
    private float jumpForce = 10f;

    private static final float PPM = 32f;

    public Player(World world, float x, float y) {
        createBody(world, x, y);
        loadAnimations();
    }

    private void createBody(World world, float x, float y) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);
        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.25f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0f;

        body.createFixture(fixtureDef);
        shape.dispose();
        body.setFixedRotation(true);
    }

    private void loadAnimations() {
        try {
            TextureRegion[] rightFrames = new TextureRegion[4];
            TextureRegion[] leftFrames = new TextureRegion[4];

            for (int i = 0; i < 4; i++) {
                Texture t = new Texture("player/step" + (i+1) + ".png");
                rightFrames[i] = new TextureRegion(t);
                leftFrames[i] = new TextureRegion(t);
                leftFrames[i].flip(true, false);
            }

            walkRightAnimation = new Animation<>(0.12f, rightFrames);
            walkLeftAnimation = new Animation<>(0.12f, leftFrames);

            Texture standTex = new Texture("player/step1.png");
            standRight = new TextureRegion(standTex);
            standLeft = new TextureRegion(standTex);
            standLeft.flip(true, false);
        } catch (Exception e) {
            // fallback
        }
    }

    public void update(float delta) {
        float velX = 0;
        if (movingRight) velX = speed;
        if (movingLeft) velX = -speed;

        body.setLinearVelocity(velX, body.getLinearVelocity().y);

        if (movingLeft || movingRight) {
            stateTime += delta;
            facingRight = movingRight;
        } else {
            stateTime = 0;
        }
    }

    public void jump() {
        if (isGrounded) {
            body.setLinearVelocity(body.getLinearVelocity().x, jumpForce);
            isGrounded = false;
        }
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean canJump() {
        return isGrounded;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion region;
        if (movingLeft) region = walkLeftAnimation.getKeyFrame(stateTime, true);
        else if (movingRight) region = walkRightAnimation.getKeyFrame(stateTime, true);
        else region = facingRight ? standRight : standLeft;

        Vector2 pos = body.getPosition();
        float size = 64f;
        batch.draw(region, pos.x * PPM - size/2, pos.y * PPM - size/2, size, size);
    }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
    public Vector2 getPosition() { return body.getPosition(); }
    public Body getBody() { return body; }
    public void setTransform(float x, float y) { body.setTransform(x, y, 0); }

    public void reset(Vector2 spawnPos) {
        body.setTransform(spawnPos.x, spawnPos.y, 0);
        body.setLinearVelocity(0, 0);
        movingLeft = false;
        movingRight = false;
        isGrounded = false;
    }
}