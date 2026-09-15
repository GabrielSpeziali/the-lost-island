package com.gabrielspeziali.thelostisland;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {
    public static final float WALK_SPEED = 150f;    // pixels/second
    public static final float RUN_SPEED = 280f;
    public static final float GRAVITY = -1000f;     // pixels/second², negative  bring down
    public static final float JUMP_HEIGHT = 150f;    // jump height / pixels
    public static final float JUMP_VELOCITY =
        (float) Math.sqrt(2 * Math.abs(GRAVITY) * JUMP_HEIGHT);      // free fall formule
    public static final float JUMP_BUFFER_TIME = 0.20f; //seconds of tolerance

    public Vector2 position;
    public Vector2 velocity;
    public float width = 28f;   // collision hitbox - SHORTER than the visual sprite
    public boolean onGround = false;
    public boolean facingRight = true;

    private int lastHorizontalKey = -1; // stores which key (A or D) was pressed last
    private float jumpBufferTimer = 0f;
    private boolean isRunning = false;

    private boolean onGroundPrevFrame = true;

    private static final float JUMP_EDGE_HOLD = 0.06f;
    private static final float JUMP_FLAP_INTERVAL = 1 / 10f;

    private enum JumpPhase { NONE, TAKEOFF, LANDING}
    private JumpPhase jumpPhase = JumpPhase.NONE;
    private float jumpPhaseTimer = 0f;

    private static final float COYOTE_TIME = 0.10f;
    private float coyoteTimer = 0f;

    private Array<TextureAtlas.AtlasRegion> jumpRightFrames, jumpLeftFrames;

    private boolean isDucking = false;
    private static final float STANDING_HEIGHT = 44f;
    private static final float DUCK_HEIGHT = 26f;

    public float height = STANDING_HEIGHT;

    public static final int MAX_LIFE = 5;
    public int currentLife = MAX_LIFE;

    private static final float ATTACK_FRAME_TIME = 1 / 12f; // duration of each of the 4 frames
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private Array<TextureAtlas.AtlasRegion> attackRightFrames, attackLeftFrames;

    // Animation
    private enum State {IDLE, WALK, RUN, JUMP, DUCK}
    private State currentState = State.IDLE;
    private State previousState = State.IDLE;
    private float stateTime = 0f;

    private TextureAtlas atlas;
    private Animation<TextureRegion> idleRight, idleLeft;
    private Animation<TextureRegion> walkRight, walkLeft;
    private Animation<TextureRegion> runRight, runLeft;
    private Animation<TextureRegion> duckRight, duckLeft;

    public Player(float startX, float startY) {
        position = new Vector2(startX, startY);
        velocity = new Vector2(0, 0);
        loadAnimations();
    }

    private void loadAnimations() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/player/player.atlas"));

        idleRight = new Animation<>(1 / 8f, atlas.findRegions("player_idle_right"), Animation.PlayMode.LOOP);
        idleLeft  = new Animation<>(1 / 8f, atlas.findRegions("player_idle_left"), Animation.PlayMode.LOOP);

        walkRight = new Animation<>(1 / 12f, atlas.findRegions("player_walk_right"), Animation.PlayMode.LOOP);
        walkLeft  = new Animation<>(1 / 12f, atlas.findRegions("player_walk_left"), Animation.PlayMode.LOOP);

        runRight = new Animation<>(1 / 16f, atlas.findRegions("player_run_right"), Animation.PlayMode.LOOP);
        runLeft  = new Animation<>(1 / 16f, atlas.findRegions("player_run_left"), Animation.PlayMode.LOOP);

        duckRight = new Animation<>(1 / 10f, atlas.findRegions("player_duck_right"), Animation.PlayMode.NORMAL);
        duckLeft  = new Animation<>(1 / 10f, atlas.findRegions("player_duck_left"), Animation.PlayMode.NORMAL);

        jumpRightFrames = atlas.findRegions("player_jump_right"); // 6 frames, index 0-5
        jumpLeftFrames = atlas.findRegions("player_jump_left");
        attackRightFrames = atlas.findRegions("player_attack_right"); // 4 frames, index 0-3
        attackLeftFrames = atlas.findRegions("player_attack_left");
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }

    public void update(float delta, Array<Rectangle> solids, float mouseWorldX) {
        onGroundPrevFrame = onGround; // snapshot before physics this frame

        handleInput(mouseWorldX);

        velocity.y += GRAVITY * delta;

        // Movement X
        float newX = position.x + velocity.x * delta;
        Rectangle boundsX = new Rectangle(newX, position.y, width, height);
        if (!collidesAny(boundsX, solids)) {
            position.x = newX;
        } else {
            velocity.x = 0;
        }

        height = isDucking ? DUCK_HEIGHT : STANDING_HEIGHT;

        // Movement Y
        float newY = position.y + velocity.y * delta;
        Rectangle boundsY = new Rectangle(position.x, newY, width, height);
        if (!collidesAny(boundsY, solids)) {
            position.y = newY;
            onGround = false;
        } else {
            if (velocity.y < 0) onGround = true;   // It only registers "on the ground" if it was falling
            velocity.y = 0;
        }

        if (onGround) {
            coyoteTimer = COYOTE_TIME;
        } else {
            coyoteTimer -= delta;
        }

        // Jump buffer: if the intention still "alive" and the player is on the ground, jump
        if (jumpBufferTimer > 0f) {
            jumpBufferTimer -= delta;
            if (onGround || coyoteTimer > 0f) {
                velocity.y = JUMP_VELOCITY;
                onGround = false;
                jumpBufferTimer = 0f;
                coyoteTimer = 0f;   // consumed, prevents a second jump off the same "grace window"
                jumpPhase = JumpPhase.TAKEOFF;
                jumpPhaseTimer = JUMP_EDGE_HOLD;
            }
        }

// Landing: was airborne last frame, grounded now
        if (!onGroundPrevFrame && onGround) {
            jumpPhase = JumpPhase.LANDING;
            jumpPhaseTimer = JUMP_EDGE_HOLD;
        }

// Independent from the buffer above, so it keeps counting down every frame
        if (jumpPhase != JumpPhase.NONE) {
            jumpPhaseTimer -= delta;
            if (jumpPhaseTimer <= 0f) jumpPhase = JumpPhase.NONE;
        }

        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= attackRightFrames.size * ATTACK_FRAME_TIME) {
                isAttacking = false;
            }
        }

        updateAnimationState(delta);
    }

    private void updateAnimationState(float delta) {
        if (isDucking) {
            currentState = State.DUCK;
        }
        else if(!onGround) {
            currentState = State.JUMP;
        } else if (velocity.x != 0) {
            currentState = isRunning ? State.RUN : State.WALK;
        } else {
            currentState = State.IDLE;
        }

        // Reset stateTime only when the state actually changes,
        // so animations restart cleanly instead of jumping to a random frame
        if (currentState != previousState) {
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
        previousState = currentState;
    }

    public TextureRegion getCurrentFrame() {
        // Takeoff/landing poses override everything else, even though the player is
        // technically grounded (currentState would say IDLE/WALK/RUN at that instant)
        if (jumpPhase == JumpPhase.TAKEOFF) {
            return facingRight ? jumpRightFrames.get(0) : jumpLeftFrames.get(0);
        }
        if (jumpPhase == JumpPhase.LANDING) {
            return facingRight ? jumpRightFrames.get(5) : jumpLeftFrames.get(5);
        }

        // Duck always wins, even over an ongoing attack
        if (currentState == State.DUCK) {
            Animation<TextureRegion> duckAnim = facingRight ? duckRight : duckLeft;
            return duckAnim.getKeyFrame(stateTime, false);
        }

        // Attack overrides jump and ground animations, but not duck (handled above)
        if (isAttacking) {
            Array<TextureAtlas.AtlasRegion> frames = facingRight ? attackRightFrames : attackLeftFrames;
            int idx = Math.min((int) (attackTimer / ATTACK_FRAME_TIME), frames.size - 1);
            return frames.get(idx);
        }

        if (currentState == State.JUMP) {
            Array<TextureAtlas.AtlasRegion> frames = facingRight ? jumpRightFrames : jumpLeftFrames;
            if (velocity.y > 0) {
                // Rising: alternate between frame 1 and 2
                int idx = 1 + ((int) (stateTime / JUMP_FLAP_INTERVAL) % 2);
                return frames.get(idx);
            } else {
                // Falling: alternate between frame 3 and 4
                int idx = 3 + ((int) (stateTime / JUMP_FLAP_INTERVAL) % 2);
                return frames.get(idx);
            }
        }

        Animation<TextureRegion> anim;
        switch (currentState) {
            case WALK:
                anim = facingRight ? walkRight : walkLeft;
                break;
            case RUN:
                anim = facingRight ? runRight : runLeft;
                break;
            default:
                anim = facingRight ? idleRight : idleLeft;
                break;
        }
        return anim.getKeyFrame(stateTime, true);
    }

    private void handleInput(float mouseWorldX) {
        isDucking = onGround && (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT));

        boolean leftHeld = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightHeld = Gdx.input.isKeyPressed(Input.Keys.D);

        // Updates the most recent key pressed, only at the moment it is pressed.
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) lastHorizontalKey = Input.Keys.A;
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) lastHorizontalKey = Input.Keys.D;

        float direction = 0;
        if (leftHeld && rightHeld) {
            // Both pressed: win the last key pressed
            direction = (lastHorizontalKey == Input.Keys.A) ? -1 : 1;
        } else if (leftHeld) {
            direction = -1;
        } else if (rightHeld) {
            direction = 1;
        }

        // Facing updates regardless of ducking, so you can still turn while crouched
        if (direction < 0) facingRight = false;
        if (direction > 0) facingRight = true;

        if (isDucking) {
            velocity.x = 0; // movement locked while ducking
        } else {
            isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
            float speed = isRunning ? RUN_SPEED : WALK_SPEED;
            velocity.x = direction * speed;
        }

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W);
        if (jumpPressed) {
            jumpBufferTimer = JUMP_BUFFER_TIME;   // keep the intention of jump
        }

        // Attack: left mouse click. Duck takes priority — no attacking while crouched.
        if (!isDucking && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float playerCenterX = position.x + width / 2f;
            facingRight = mouseWorldX >= playerCenterX; // turn to face the mouse side, even mid-movement

            isAttacking = true;
            attackTimer = 0f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            takeDamage(); // temporary: press K to test the life HUD
        }
    }

    private boolean collidesAny(Rectangle bounds, Array<Rectangle> solids) {
        for (Rectangle r : solids) {
            if (bounds.overlaps(r)) return true;
        }
        return false;
    }

    public void takeDamage() {
        if (currentLife > 0) {
            currentLife--;
        }
    }

    public boolean isDead() {
        return currentLife <= 0;
    }

    public void dispose() {
        atlas.dispose();
    }
}
