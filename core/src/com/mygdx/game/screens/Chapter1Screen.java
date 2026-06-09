package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.screens.Chapter2Screen;
import com.mygdx.game.screens.StartMenuScreen;
import java.util.HashMap;
import java.util.Map;

public class Chapter1Screen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private World world;
    private static final float PPM = 32f;
    private static final float ZOOM = 3.0f;
    private static final float GRAVITY = 0f;

    private Body playerBody;
    private TextureRegion[] walkRightFrames;
    private TextureRegion[] walkLeftFrames;
    private TextureRegion standRight, standLeft;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private float stateTime;
    private boolean facingRight = true;
    private float speed = 5f;
    private String currentMap;
    private boolean isTransitioning = false;
    private String returnDoorId = null;
    private String startMap;
    private String startSpawnId;

    // JOYSTICK
    private Texture joystickBaseTexture;
    private Texture joystickKnobTexture;
    private Vector2 joystickBasePos;
    private Vector2 joystickKnobPos;
    private float joystickBaseRadius = 70f;
    private float joystickKnobRadius = 35f;
    private boolean joystickActive = false;
    private int joystickPointer = -1;
    private Vector2 joystickDirection = new Vector2(0, 0);
    private float screenW, screenH;

    // UI STAGES
    private Stage uiStage;
    private Stage pauseStage;
    private Stage messageStage;
    private Stage imageStage;
    private Stage bookStage;
    private boolean isPaused = false;
    private boolean showInteractionBtn = false;
    private String pendingDoorId = null;
    private String pendingItemId = null;
    private Body pendingItemBody = null;
    private Body pendingInteractiveBody = null;
    private String pendingInteractiveName = null;
    private Body pendingMaBody = null;

    // BUTTONS
    private ImageButton pauseBtn, interactionBtn, bookBtn;
    private Texture pauseTex, interactionTex, bookTex;

    // ITEMS & NOTES
    private Label itemsLabel;
    private int collectedItems = 0;
    private int totalItems = 5;
    private Map<Body, String> itemBodies = new HashMap<>();
    private Array<Body> bodiesToDestroy = new Array<>();
    private boolean allItemsCollected = false;
    private Map<Body, String> interactiveBodies = new HashMap<>();

    private Texture currentImageTexture = null;
    private Texture currentNoteTexture = null;
    private boolean showingImage = false;
    private Array<String> collectedNotes = new Array<>();
    private int currentNoteIndex = 0;
    private boolean showingBook = false;

    private float saveTimer = 0f;
    private static final float SAVE_INTERVAL = 5f;

    private float uiScale;
    private int btnSize;
    private int pauseSize;

    public Chapter1Screen(TheFateGame game) {
        this(game, "room3.tmx", "spawn");
    }

    public Chapter1Screen(TheFateGame game, String startMap, String startSpawnId) {
        this.game = game;
        this.startMap = startMap;
        this.startSpawnId = startSpawnId;
        this.currentMap = startMap;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();

        this.uiScale = game.getUIScale();
        this.btnSize = game.getScaledSize(100);
        this.pauseSize = game.getScaledSize(70);

        float worldWidth = TheFateGame.VIRTUAL_WIDTH / ZOOM;
        float worldHeight = TheFateGame.VIRTUAL_HEIGHT / ZOOM;
        this.camera.setToOrtho(false, worldWidth, worldHeight);
        this.camera.zoom = ZOOM;

        this.screenW = TheFateGame.VIRTUAL_WIDTH;
        this.screenH = TheFateGame.VIRTUAL_HEIGHT;

        this.uiStage = new Stage(new ExtendViewport(screenW, screenH));
        this.pauseStage = new Stage(new ExtendViewport(screenW, screenH));
        this.messageStage = new Stage(new ExtendViewport(screenW, screenH));
        this.imageStage = new Stage(new ExtendViewport(screenW, screenH));
        this.bookStage = new Stage(new ExtendViewport(screenW, screenH));

        loadCollectedNotes();
        collectedItems = game.prefs.getInteger("chapter1_items", 0);
        allItemsCollected = collectedItems >= totalItems;

        loadTextures();
        createUI();
        loadMap(currentMap);
        createWorld();
        createPlayer();
        createCollisionAndTeleports();

        Gdx.input.setInputProcessor(uiStage);
        game.stopMenuMusic();
        game.startGameMusic();
    }

    private void loadCollectedNotes() {
        collectedNotes.clear();
        for (int i = 1; i <= 10; i++) {
            if (game.prefs.getBoolean("note_z" + i, false)) {
                collectedNotes.add("z" + i);
            }
        }
        collectedNotes.sort();
    }

    private void saveNote(String noteId) {
        if (!collectedNotes.contains(noteId, false)) {
            collectedNotes.add(noteId);
            collectedNotes.sort();
            game.prefs.putBoolean("note_z" + Integer.parseInt(noteId.substring(1)), true);
            game.prefs.flush();
        }
    }

    private String getNoteImagePath(String noteId) {
        String currentLang = game.languageManager.getCurrentLanguage();
        String imagePath = "notes/" + currentLang + "/" + noteId + ".png";

        if (!Gdx.files.internal(imagePath).exists()) {
            imagePath = "notes/ru/" + noteId + ".png";
        }

        return imagePath;
    }

    private void showNoteImage(String noteId) {
        String imagePath = getNoteImagePath(noteId);

        if (!Gdx.files.internal(imagePath).exists()) {
            showNoteText(noteId);
            return;
        }

        try {
            if (currentImageTexture != null) {
                currentImageTexture.dispose();
            }
            currentImageTexture = new Texture(imagePath);
            showingImage = true;

            imageStage.clear();
            Table darkBg = new Table();
            darkBg.setFillParent(true);
            darkBg.setColor(0, 0, 0, 0.9f);
            imageStage.addActor(darkBg);

            Table table = new Table();
            table.setFillParent(true);
            imageStage.addActor(table);

            int originalWidth = currentImageTexture.getWidth();
            int originalHeight = currentImageTexture.getHeight();

            float maxWidth = screenW * 0.85f;
            float maxHeight = screenH * 0.8f;

            float scaleX = maxWidth / originalWidth;
            float scaleY = maxHeight / originalHeight;
            float scale = Math.min(scaleX, scaleY);

            float displayWidth = originalWidth * scale;
            float displayHeight = originalHeight * scale;

            Image displayedImage = new Image(currentImageTexture);
            displayedImage.setSize(displayWidth, displayHeight);

            Table imageContainer = new Table();
            imageContainer.add(displayedImage).center();
            table.add(imageContainer).center().padBottom(20);

            Label continueLabel = new Label(game.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
                font = game.font;
                fontColor = com.badlogic.gdx.graphics.Color.WHITE;
            }});
            continueLabel.setFontScale(1.2f);
            table.add(continueLabel).padTop(20).center();

            imageStage.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    hideCurrentImage();
                    return true;
                }
            });

            Gdx.input.setInputProcessor(imageStage);

        } catch (Exception e) {
            hideCurrentImage();
            showNoteText(noteId);
        }
    }

    private void showNoteText(String noteId) {
        String noteText = game.languageManager.getText("note_" + noteId);

        imageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        imageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        imageStage.addActor(table);

        Table textContainer = new Table();
        textContainer.pad(30);

        int noteNumber = Integer.parseInt(noteId.substring(1));
        Label titleLabel = new Label(game.languageManager.getText("note_prefix") + noteNumber, new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        textContainer.add(titleLabel).padBottom(20).row();

        Label textLabel = new Label(noteText, new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        textLabel.setWrap(true);
        textContainer.add(textLabel).width(screenW * 0.7f).padBottom(30).row();

        Label continueLabel = new Label(game.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
            font = game.font;
            fontColor = com.badlogic.gdx.graphics.Color.CYAN;
        }});
        textContainer.add(continueLabel);

        table.add(textContainer).center();

        imageStage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                hideCurrentImage();
                return true;
            }
        });

        Gdx.input.setInputProcessor(imageStage);
    }

    private void hideCurrentImage() {
        showingImage = false;
        imageStage.clear();
        if (currentImageTexture != null) {
            currentImageTexture.dispose();
            currentImageTexture = null;
        }
        Gdx.input.setInputProcessor(uiStage);
    }

    private void showBook() {
        if (collectedNotes.size == 0) {
            showMessage(game.languageManager.getText("no_notes"), 1.5f);
            return;
        }
        showingBook = true;
        currentNoteIndex = 0;
        showCurrentNote();
        Gdx.input.setInputProcessor(bookStage);
    }

    private ScrollPane createTextScrollPane(String text) {
        Table textTable = new Table();
        textTable.pad(20);

        Label textLabel = new Label(text, new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        textLabel.setWrap(true);
        textTable.add(textLabel).width(screenW * 0.65f);

        ScrollPane scrollPane = new ScrollPane(textTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    private void showCurrentNote() {
        bookStage.clear();

        String noteId = collectedNotes.get(currentNoteIndex);
        int noteNumber = Integer.parseInt(noteId.substring(1));

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.95f);
        bookStage.addActor(darkBg);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        bookStage.addActor(mainTable);

        Table contentTable = new Table();
        contentTable.center();

        Label titleLabel = new Label(game.languageManager.getText("note_prefix") + noteNumber, new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).padBottom(25).row();

        String imagePath = getNoteImagePath(noteId);
        boolean imageLoaded = false;

        if (Gdx.files.internal(imagePath).exists()) {
            try {
                if (currentNoteTexture != null) {
                    currentNoteTexture.dispose();
                }
                currentNoteTexture = new Texture(imagePath);
                int originalWidth = currentNoteTexture.getWidth();
                int originalHeight = currentNoteTexture.getHeight();

                float maxWidth = screenW * 0.75f;
                float maxHeight = screenH * 0.55f;

                float scaleX = maxWidth / originalWidth;
                float scaleY = maxHeight / originalHeight;
                float scale = Math.min(scaleX, scaleY);

                float displayWidth = originalWidth * scale;
                float displayHeight = originalHeight * scale;

                Image noteImage = new Image(currentNoteTexture);
                noteImage.setSize(displayWidth, displayHeight);

                Table imageContainer = new Table();
                imageContainer.add(noteImage).center();
                contentTable.add(imageContainer).center().padBottom(30);
                imageLoaded = true;
            } catch (Exception e) {
                imageLoaded = false;
            }
        }

        if (!imageLoaded) {
            String noteText = game.languageManager.getText("note_" + noteId);
            ScrollPane scrollPane = createTextScrollPane(noteText);
            contentTable.add(scrollPane).width(screenW * 0.7f).height(screenH * 0.5f).padBottom(30);
        }

        Table navTable = new Table();
        navTable.center();

        if (currentNoteIndex > 0) {
            TextButton prevBtn = new TextButton("← " + game.languageManager.getText("previous"), new TextButton.TextButtonStyle() {{
                font = game.smallFont;
            }});
            prevBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent e, float x, float y) {
                    if (currentNoteTexture != null) {
                        currentNoteTexture.dispose();
                        currentNoteTexture = null;
                    }
                    currentNoteIndex--;
                    showCurrentNote();
                }
            });
            navTable.add(prevBtn).width(160).height(50).padRight(25);
        }

        Label pageLabel = new Label(game.languageManager.getText("page") + " " + (currentNoteIndex + 1) + " " +
                game.languageManager.getText("of") + " " + collectedNotes.size, new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        navTable.add(pageLabel).padLeft(15).padRight(15);

        if (currentNoteIndex < collectedNotes.size - 1) {
            TextButton nextBtn = new TextButton(game.languageManager.getText("next") + " →", new TextButton.TextButtonStyle() {{
                font = game.smallFont;
            }});
            nextBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent e, float x, float y) {
                    if (currentNoteTexture != null) {
                        currentNoteTexture.dispose();
                        currentNoteTexture = null;
                    }
                    currentNoteIndex++;
                    showCurrentNote();
                }
            });
            navTable.add(nextBtn).width(160).height(50).padLeft(25);
        }

        contentTable.add(navTable).padBottom(30).row();

        TextButton closeBtn = new TextButton(game.languageManager.getText("close"), new TextButton.TextButtonStyle() {{
            font = game.smallFont;
        }});
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (currentNoteTexture != null) {
                    currentNoteTexture.dispose();
                    currentNoteTexture = null;
                }
                showingBook = false;
                bookStage.clear();
                Gdx.input.setInputProcessor(uiStage);
            }
        });
        contentTable.add(closeBtn).width(150).height(50);

        mainTable.add(contentTable).center();
    }

    private void showMessage(String msg, float duration) {
        messageStage.clear();
        Table bg = new Table();
        bg.setFillParent(true);
        bg.setColor(0, 0, 0, 0.7f);
        messageStage.addActor(bg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label label = new Label(msg, new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        }});
        label.setFontScale(1.2f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() { messageStage.clear(); }
        }, duration);
    }

    private void loadTextures() {
        try {
            Texture standTex = new Texture("player/step1.png");
            standRight = new TextureRegion(standTex);
            standLeft = new TextureRegion(standTex);
            standLeft.flip(true, false);
            walkRightFrames = new TextureRegion[4];
            walkLeftFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                Texture t = new Texture("player/step" + (i+1) + ".png");
                walkRightFrames[i] = new TextureRegion(t);
                walkLeftFrames[i] = new TextureRegion(t);
                walkLeftFrames[i].flip(true, false);
            }
            walkRightAnimation = new Animation<TextureRegion>(0.12f, walkRightFrames);
            walkLeftAnimation = new Animation<TextureRegion>(0.12f, walkLeftFrames);
        } catch (Exception e) {
            Texture fallback = createFallbackTexture(0.5f, 0.5f, 0.5f);
            standRight = standLeft = new TextureRegion(fallback);
        }

        pauseTex = loadTextureWithFallback("button/button_pause.png", 0.8f, 0.8f, 0.2f);
        interactionTex = loadTextureWithFallback("button/button_interaction.png", 0.2f, 0.8f, 0.2f);
        bookTex = loadTextureWithFallback("item/book3.png", 0.6f, 0.4f, 0.2f);

        joystickBaseTexture = createJoystickBaseTexture();
        joystickKnobTexture = createJoystickKnobTexture();

        joystickBasePos = new Vector2(150 * uiScale, 150 * uiScale);
        joystickKnobPos = new Vector2(joystickBasePos.x, joystickBasePos.y);
    }

    private Texture createJoystickBaseTexture() {
        int size = (int)(joystickBaseRadius * 2);
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.4f, 0.6f);
        pixmap.fillCircle((int)joystickBaseRadius, (int)joystickBaseRadius, (int)joystickBaseRadius);
        pixmap.setColor(0.5f, 0.5f, 0.6f, 0.8f);
        pixmap.drawCircle((int)joystickBaseRadius, (int)joystickBaseRadius, (int)(joystickBaseRadius - 2));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createJoystickKnobTexture() {
        int size = (int)(joystickKnobRadius * 2);
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.8f, 0.8f, 0.9f, 0.9f);
        pixmap.fillCircle((int)joystickKnobRadius, (int)joystickKnobRadius, (int)joystickKnobRadius);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.drawCircle((int)joystickKnobRadius, (int)joystickKnobRadius, (int)(joystickKnobRadius - 3));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture loadTextureWithFallback(String path, float r, float g, float b) {
        try { return new Texture(path); }
        catch (Exception e) { return createFallbackTexture(r, g, b); }
    }

    private Texture createFallbackTexture(float r, float g, float b) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void createUI() {
        float topMargin = 20 * uiScale;

        pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - topMargin, screenH - pauseSize - topMargin);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!showingImage && !showingBook) {
                    isPaused = true;
                    createPauseDialog();
                    Gdx.input.setInputProcessor(pauseStage);
                    if (game.gameMusic != null) game.gameMusic.pause();
                }
            }
        });

        bookBtn = new ImageButton(new TextureRegionDrawable(bookTex));
        bookBtn.setSize(pauseSize, pauseSize);
        bookBtn.setPosition(screenW - (pauseSize * 2) - (topMargin * 1.5f), screenH - pauseSize - topMargin);
        bookBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!showingImage && !showingBook && !isPaused && !isTransitioning) {
                    showBook();
                }
            }
        });

        interactionBtn = new ImageButton(new TextureRegionDrawable(interactionTex));
        interactionBtn.setSize(game.getScaledSize(90), game.getScaledSize(90));
        interactionBtn.setPosition(screenW / 2 - game.getScaledSize(45), screenH / 2 - game.getScaledSize(60));
        interactionBtn.setVisible(false);
        interactionBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isPaused && !isTransitioning && showInteractionBtn && !showingImage && !showingBook) {
                    if (pendingDoorId != null) {
                        teleportToDoor(pendingDoorId);
                    } else if (pendingItemBody != null && pendingItemId != null) {
                        collectItem(pendingItemBody, pendingItemId);
                    } else if (pendingInteractiveBody != null && pendingInteractiveName != null) {
                        if (pendingInteractiveName.startsWith("z")) {
                            saveNote(pendingInteractiveName);
                            showNoteImage(pendingInteractiveName);
                            bodiesToDestroy.add(pendingInteractiveBody);
                            interactiveBodies.remove(pendingInteractiveBody);
                        } else {
                            showImageForObject(pendingInteractiveName);
                        }
                        showInteractionBtn = false;
                        interactionBtn.setVisible(false);
                        pendingInteractiveBody = null;
                        pendingInteractiveName = null;
                    } else if (pendingMaBody != null && allItemsCollected) {
                        showToBeContinuedAndExit();
                        showInteractionBtn = false;
                        interactionBtn.setVisible(false);
                        pendingMaBody = null;
                    }
                }
            }
        });

        uiStage.addActor(pauseBtn);
        uiStage.addActor(bookBtn);
        uiStage.addActor(interactionBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.smallFont;
        itemsLabel = new Label("", labelStyle);
        itemsLabel.setPosition(20 * uiScale, screenH - 50 * uiScale);
        uiStage.addActor(itemsLabel);
        updateItemsLabel();
    }

    private void updateJoystick() {
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float touchX = Gdx.input.getX(i);
                float touchY = Gdx.input.getY(i);

                Vector3 stageCoords = uiStage.getViewport().unproject(new Vector3(touchX, touchY, 0));
                float x = stageCoords.x;
                float y = stageCoords.y;

                if (!joystickActive && !isPaused && !isTransitioning && !showingImage && !showingBook) {
                    float dx = x - joystickBasePos.x;
                    float dy = y - joystickBasePos.y;
                    float dist = (float)Math.sqrt(dx * dx + dy * dy);

                    if (dist <= joystickBaseRadius + 20) {
                        joystickActive = true;
                        joystickPointer = i;
                    }
                }

                if (joystickActive && joystickPointer == i) {
                    float dx = x - joystickBasePos.x;
                    float dy = y - joystickBasePos.y;
                    float dist = (float)Math.sqrt(dx * dx + dy * dy);

                    if (dist > joystickBaseRadius) {
                        dx = dx / dist * joystickBaseRadius;
                        dy = dy / dist * joystickBaseRadius;
                        dist = joystickBaseRadius;
                    }

                    joystickKnobPos.set(joystickBasePos.x + dx, joystickBasePos.y + dy);

                    if (dist > 5) {
                        joystickDirection.set(dx / joystickBaseRadius, dy / joystickBaseRadius);
                    } else {
                        joystickDirection.set(0, 0);
                    }
                    return;
                }
            }
        }

        if (joystickActive) {
            joystickActive = false;
            joystickPointer = -1;
            joystickDirection.set(0, 0);
            joystickKnobPos.set(joystickBasePos.x, joystickBasePos.y);
        }
    }

    private void updateItemsLabel() {
        itemsLabel.setText(game.languageManager.format("items_collected", collectedItems, totalItems));
    }

    private void collectItem(Body itemBody, String itemId) {
        if (!itemId.equals("collected")) {
            collectedItems++;
            updateItemsLabel();
            game.prefs.putInteger("chapter1_items", collectedItems);
            game.prefs.flush();
            bodiesToDestroy.add(itemBody);
            itemBodies.put(itemBody, "collected");
            pendingItemBody = null;
            pendingItemId = null;
            showInteractionBtn = false;
            interactionBtn.setVisible(false);
            if (collectedItems >= totalItems) {
                allItemsCollected = true;
                showAllItemsCollectedMessage();
                game.prefs.putBoolean("chapter2_unlocked", true);
                game.prefs.flush();
            }
            saveProgress();
        }
    }

    private void saveProgress() {
        game.saveGameProgress(currentMap, collectedItems);
        game.prefs.flush();
    }

    private void showAllItemsCollectedMessage() {
        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.8f);
        messageStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);
        Label label = new Label(game.languageManager.getText("all_items_collected"), new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = com.badlogic.gdx.graphics.Color.GREEN;
        }});
        label.setFontScale(1.2f);
        table.add(label).center();
        Timer.schedule(new Timer.Task() {
            @Override public void run() { messageStage.clear(); }
        }, 3);
    }

    private void showToBeContinuedAndExit() {
        isTransitioning = true;
        showInteractionBtn = false;
        interactionBtn.setVisible(false);

        messageStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.9f);
        messageStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        messageStage.addActor(table);

        Label titleLabel = new Label(game.languageManager.getText("chapter1_complete_title"), new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = com.badlogic.gdx.graphics.Color.GOLD;
        }});
        titleLabel.setFontScale(1.8f);
        Label subtitleLabel = new Label(game.languageManager.getText("chapter2_unlocked"), new Label.LabelStyle() {{
            font = game.font;
        }});
        subtitleLabel.setFontScale(1.2f);
        Label loadingLabel = new Label(game.languageManager.getText("loading"), new Label.LabelStyle() {{
            font = game.font;
        }});

        table.add(titleLabel).center().padBottom(20);
        table.row();
        table.add(subtitleLabel).center().padBottom(10);
        table.row();
        table.add(loadingLabel).center();

        game.clearSave();
        game.stopGameMusic();
        game.prefs.putBoolean("chapter2_unlocked", true);
        game.prefs.flush();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                messageStage.clear();
                game.setScreen(new Chapter2Screen(game));
            }
        }, 2);
    }

    private void createPauseDialog() {
        pauseStage.clear();
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        pauseStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        pauseStage.addActor(table);

        Table dialog = new Table();
        dialog.pad(30);
        Label title = new Label(game.languageManager.getText("game_paused"), new Label.LabelStyle() {{ font = game.font; }});
        title.setFontScale(2f);
        TextButton continueBtn = new TextButton(game.languageManager.getText("resume"), new TextButton.TextButtonStyle() {{ font = game.font; }});
        continueBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                pauseStage.clear();
                Gdx.input.setInputProcessor(uiStage);
                if (game.gameMusic != null && game.musicEnabled) game.gameMusic.play();
            }
        });
        TextButton exitBtn = new TextButton(game.languageManager.getText("exit_to_menu"), new TextButton.TextButtonStyle() {{ font = game.font; }});
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                saveProgress();
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new StartMenuScreen(game));
            }
        });
        dialog.add(title).padBottom(40).row();
        dialog.add(continueBtn).width(game.getScaledSize(250)).height(game.getScaledSize(60)).padBottom(20).row();
        dialog.add(exitBtn).width(game.getScaledSize(250)).height(game.getScaledSize(60)).row();
        table.add(dialog).center();
    }

    private void showImageForObject(String objectName) {
        String imagePath = objectName + ".png";
        try {
            if (currentImageTexture != null) {
                currentImageTexture.dispose();
            }
            currentImageTexture = new Texture(imagePath);
            showingImage = true;

            imageStage.clear();
            Table darkBg = new Table();
            darkBg.setFillParent(true);
            darkBg.setColor(0, 0, 0, 0.85f);
            imageStage.addActor(darkBg);

            Table table = new Table();
            table.setFillParent(true);
            imageStage.addActor(table);

            int originalWidth = currentImageTexture.getWidth();
            int originalHeight = currentImageTexture.getHeight();
            float maxWidth = screenW * 0.8f;
            float maxHeight = screenH * 0.8f;
            float scale = Math.min(maxWidth / originalWidth, maxHeight / originalHeight);

            Image displayedImage = new Image(currentImageTexture);
            displayedImage.setSize(originalWidth * scale, originalHeight * scale);

            Table imageContainer = new Table();
            imageContainer.add(displayedImage).center();
            table.add(imageContainer).center();

            Label continueLabel = new Label(game.languageManager.getText("tap_to_continue"), new Label.LabelStyle() {{
                font = game.font;
            }});
            continueLabel.setFontScale(1.2f);
            table.add(continueLabel).padTop(30).center();

            imageStage.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    hideCurrentImage();
                    return true;
                }
            });

            Gdx.input.setInputProcessor(imageStage);

        } catch (Exception e) {
            hideCurrentImage();
        }
    }

    private void loadMap(String mapPath) {
        try {
            if (tiledMap != null) tiledMap.dispose();
            tiledMap = new TmxMapLoader().load(mapPath);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            currentMap = mapPath;
        } catch (Exception e) {
        }
    }

    private void createWorld() {
        if (world != null) world.dispose();
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if ((a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) ||
                        (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected"))) {
                    pendingItemBody = (a == playerBody) ? b : a;
                    pendingItemId = itemBodies.get(pendingItemBody);
                    pendingDoorId = null;
                    pendingInteractiveBody = null;
                    pendingMaBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && isDoor(b)) || (b == playerBody && isDoor(a))) {
                    pendingDoorId = (String) ((a == playerBody) ? b.getUserData() : a.getUserData());
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingInteractiveBody = null;
                    pendingMaBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && interactiveBodies.containsKey(b)) || (b == playerBody && interactiveBodies.containsKey(a))) {
                    pendingDoorId = null;
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = (a == playerBody) ? b : a;
                    pendingInteractiveName = interactiveBodies.get(pendingInteractiveBody);
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && isMaExit(b)) || (b == playerBody && isMaExit(a))) {
                    if (allItemsCollected) {
                        pendingDoorId = null;
                        pendingItemBody = null;
                        pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = (a == playerBody) ? b : a;
                        showInteractionBtn = true;
                        interactionBtn.setVisible(true);
                    }
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b) || interactiveBodies.containsKey(b) || isMaExit(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a) || interactiveBodies.containsKey(a) || isMaExit(a)))) {
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                    pendingDoorId = null;
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingInteractiveBody = null;
                    pendingInteractiveName = null;
                    pendingMaBody = null;
                }
            }

            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private boolean isDoor(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).startsWith("door_");
    }

    private boolean isMaExit(Body body) {
        Object data = body.getUserData();
        return data instanceof String && ((String) data).equals("ma_exit");
    }

    private void teleportToDoor(String doorId) {
        showInteractionBtn = false;
        interactionBtn.setVisible(false);

        String targetMap = null, targetSpawnId = null;
        if (doorId.equals("door_exit")) {
            targetMap = "corid.tmx";
            targetSpawnId = returnDoorId == null ? "center" : returnDoorId;
        } else if (doorId.startsWith("door_door")) {
            if (currentMap.equals("corid.tmx")) {
                String roomNumber = doorId.substring(9);
                targetMap = "room" + roomNumber + ".tmx";
                targetSpawnId = "start_" + roomNumber;
                returnDoorId = "door" + roomNumber;
            }
        }

        if (targetMap == null || targetSpawnId == null) {
            return;
        }

        Gdx.input.setInputProcessor(null);
        loadMap(targetMap);
        recreateWorld();
        createPlayer();
        createCollisionAndTeleports();
        returnDoorId = returnDoorId;
        Vector2 spawnPos = findSpawnPosition(targetSpawnId);
        if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
        saveProgress();
        Gdx.input.setInputProcessor(uiStage);
    }

    private Vector2 findSpawnPosition(String spawnId) {
        if (spawnId != null && spawnId.equals("center")) return new Vector2(640 / PPM, 360 / PPM);
        if (spawnId == null) return new Vector2(640 / PPM, 360 / PPM);
        for (MapLayer layer : tiledMap.getLayers()) {
            String layerName = layer.getName();
            if (layerName != null && (layerName.equals("start") || layerName.equals("spawn"))) {
                for (MapObject obj : layer.getObjects()) {
                    String name = obj.getName();
                    if (name != null && name.equals(spawnId)) {
                        if (obj instanceof RectangleMapObject) {
                            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                            return new Vector2((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        } else {
                            Float x = obj.getProperties().get("x", Float.class);
                            Float y = obj.getProperties().get("y", Float.class);
                            if (x != null && y != null) return new Vector2(x / PPM, y / PPM);
                        }
                    }
                }
            }
        }
        return new Vector2(640 / PPM, 360 / PPM);
    }

    private void destroyMarkedBodies() {
        for (Body body : bodiesToDestroy) {
            if (body != null && body.getWorld() == world) world.destroyBody(body);
        }
        bodiesToDestroy.clear();
    }

    private void recreateWorld() {
        if (world != null) {
            world.dispose();
        }
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if ((a == playerBody && itemBodies.containsKey(b) && !itemBodies.get(b).equals("collected")) ||
                        (b == playerBody && itemBodies.containsKey(a) && !itemBodies.get(a).equals("collected"))) {
                    pendingItemBody = (a == playerBody) ? b : a;
                    pendingItemId = itemBodies.get(pendingItemBody);
                    pendingDoorId = null;
                    pendingInteractiveBody = null;
                    pendingMaBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && isDoor(b)) || (b == playerBody && isDoor(a))) {
                    pendingDoorId = (String) ((a == playerBody) ? b.getUserData() : a.getUserData());
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingInteractiveBody = null;
                    pendingMaBody = null;
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && interactiveBodies.containsKey(b)) || (b == playerBody && interactiveBodies.containsKey(a))) {
                    pendingDoorId = null;
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingMaBody = null;
                    pendingInteractiveBody = (a == playerBody) ? b : a;
                    pendingInteractiveName = interactiveBodies.get(pendingInteractiveBody);
                    showInteractionBtn = true;
                    interactionBtn.setVisible(true);
                }
                else if ((a == playerBody && isMaExit(b)) || (b == playerBody && isMaExit(a))) {
                    if (allItemsCollected) {
                        pendingDoorId = null;
                        pendingItemBody = null;
                        pendingItemId = null;
                        pendingInteractiveBody = null;
                        pendingMaBody = (a == playerBody) ? b : a;
                        showInteractionBtn = true;
                        interactionBtn.setVisible(true);
                    }
                }
            }

            @Override public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if ((a == playerBody && (isDoor(b) || itemBodies.containsKey(b) || interactiveBodies.containsKey(b) || isMaExit(b))) ||
                        (b == playerBody && (isDoor(a) || itemBodies.containsKey(a) || interactiveBodies.containsKey(a) || isMaExit(a)))) {
                    showInteractionBtn = false;
                    interactionBtn.setVisible(false);
                    pendingDoorId = null;
                    pendingItemBody = null;
                    pendingItemId = null;
                    pendingInteractiveBody = null;
                    pendingInteractiveName = null;
                    pendingMaBody = null;
                }
            }

            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });
    }

    private void createPlayer() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(640 / PPM, 360 / PPM);
        playerBody = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.setRadius(0.20f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        playerBody.setFixedRotation(true);
    }

    private void createCollisionAndTeleports() {
        MapLayer collisionLayer = tiledMap.getLayers().get("collision");
        if (collisionLayer != null) {
            for (MapObject obj : collisionLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                body.createFixture(shape, 0);
                body.setUserData("wall");
                shape.dispose();
            }
        }

        for (MapLayer layer : tiledMap.getLayers()) {
            String layerName = layer.getName();
            if (layerName != null && layerName.startsWith("door")) {
                for (MapObject obj : layer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;
                    BodyDef def = new BodyDef();
                    def.type = BodyDef.BodyType.StaticBody;
                    def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                    Body body = world.createBody(def);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                    Fixture fix = body.createFixture(shape, 0);
                    fix.setSensor(true);
                    shape.dispose();
                    body.setUserData("door_" + layerName);
                }
            }
        }

        MapLayer exitLayer = tiledMap.getLayers().get("exit");
        if (exitLayer != null) {
            for (MapObject obj : exitLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                body.setUserData("door_exit");
            }
        }

        String[] itemLayers = {"item1", "item2", "item3", "item4", "item5"};
        for (String itemLayerName : itemLayers) {
            MapLayer itemLayer = tiledMap.getLayers().get(itemLayerName);
            if (itemLayer != null) {
                for (MapObject obj : itemLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;
                    String itemId = currentMap + "_" + itemLayerName;
                    boolean alreadyCollected = game.prefs.getBoolean("item_" + itemId, false);
                    if (!alreadyCollected && collectedItems < totalItems) {
                        BodyDef def = new BodyDef();
                        def.type = BodyDef.BodyType.StaticBody;
                        def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        Body body = world.createBody(def);
                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                        Fixture fix = body.createFixture(shape, 0);
                        fix.setSensor(true);
                        shape.dispose();
                        itemBodies.put(body, itemId);
                    }
                }
            }
        }

        for (int i = 1; i <= 10; i++) {
            String layerName = "z" + i;
            MapLayer noteLayer = tiledMap.getLayers().get(layerName);
            if (noteLayer != null) {
                for (MapObject obj : noteLayer.getObjects()) {
                    Rectangle rect = getObjectRectangle(obj);
                    if (rect == null) continue;
                    String noteId = "z" + i;
                    boolean alreadyRead = game.prefs.getBoolean("note_z" + i, false);
                    if (!alreadyRead) {
                        BodyDef def = new BodyDef();
                        def.type = BodyDef.BodyType.StaticBody;
                        def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                        Body body = world.createBody(def);
                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                        Fixture fix = body.createFixture(shape, 0);
                        fix.setSensor(true);
                        shape.dispose();
                        interactiveBodies.put(body, noteId);
                    }
                }
            }
        }

        MapLayer maLayer = tiledMap.getLayers().get("ma");
        if (maLayer != null) {
            for (MapObject obj : maLayer.getObjects()) {
                Rectangle rect = getObjectRectangle(obj);
                if (rect == null) continue;
                BodyDef def = new BodyDef();
                def.type = BodyDef.BodyType.StaticBody;
                def.position.set((rect.x + rect.width/2) / PPM, (rect.y + rect.height/2) / PPM);
                Body body = world.createBody(def);
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2 / PPM, rect.height/2 / PPM);
                Fixture fix = body.createFixture(shape, 0);
                fix.setSensor(true);
                shape.dispose();
                body.setUserData("ma_exit");
            }
        }

        Vector2 spawnPos = findSpawnPosition(startSpawnId);
        if (spawnPos != null) playerBody.setTransform(spawnPos.x, spawnPos.y, 0);
    }

    private Rectangle getObjectRectangle(MapObject obj) {
        if (obj instanceof RectangleMapObject) return ((RectangleMapObject) obj).getRectangle();
        Float x = obj.getProperties().get("x", Float.class);
        Float y = obj.getProperties().get("y", Float.class);
        Float width = obj.getProperties().get("width", Float.class);
        Float height = obj.getProperties().get("height", Float.class);
        if (x != null && y != null) {
            float w = (width != null) ? width : 100f;
            float h = (height != null) ? height : 100f;
            return new Rectangle(x, y, w, h);
        }
        return null;
    }

    private void update(float delta) {
        if (isPaused || isTransitioning || showingImage || showingBook) return;

        updateJoystick();

        float velX = joystickDirection.x * speed;
        float velY = joystickDirection.y * speed;

        playerBody.setLinearVelocity(velX, velY);

        if (Math.abs(velX) > 0.1f || Math.abs(velY) > 0.1f) {
            stateTime += delta;
            facingRight = velX > 0;
        } else {
            stateTime = 0;
        }

        world.step(delta, 6, 2);
        destroyMarkedBodies();

        Vector2 pos = playerBody.getPosition();
        camera.position.set(pos.x * PPM, pos.y * PPM, 0);
        camera.update();

        saveTimer += delta;
        if (saveTimer >= SAVE_INTERVAL) {
            saveTimer = 0f;
            saveProgress();
        }
    }

    private void drawPlayer() {
        TextureRegion region;
        if (Math.abs(joystickDirection.x) > 0.1f || Math.abs(joystickDirection.y) > 0.1f) {
            if (joystickDirection.x < 0) region = walkLeftAnimation.getKeyFrame(stateTime, true);
            else region = walkRightAnimation.getKeyFrame(stateTime, true);
        } else {
            region = facingRight ? standRight : standLeft;
        }
        Vector2 pos = playerBody.getPosition();
        float size = 64f;
        batch.draw(region, pos.x * PPM - size/2, pos.y * PPM - size/2, size, size);
    }

    private void drawJoystick() {
        if (joystickBaseTexture != null) {
            batch.draw(joystickBaseTexture,
                    joystickBasePos.x - joystickBaseRadius,
                    joystickBasePos.y - joystickBaseRadius,
                    joystickBaseRadius * 2, joystickBaseRadius * 2);
        }
        if (joystickKnobTexture != null) {
            batch.draw(joystickKnobTexture,
                    joystickKnobPos.x - joystickKnobRadius,
                    joystickKnobPos.y - joystickKnobRadius,
                    joystickKnobRadius * 2, joystickKnobRadius * 2);
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPlayer();
        batch.end();

        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        drawJoystick();
        batch.end();

        uiStage.act(delta);
        uiStage.draw();

        if (isPaused) {
            pauseStage.act(delta);
            pauseStage.draw();
        }

        messageStage.act(delta);
        messageStage.draw();

        if (showingImage) {
            imageStage.act(delta);
            imageStage.draw();
        }

        if (showingBook) {
            bookStage.act(delta);
            bookStage.draw();
        }
    }

    @Override public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        float worldWidth = game.viewport.getWorldWidth() / ZOOM;
        float worldHeight = game.viewport.getWorldHeight() / ZOOM;
        camera.viewportWidth = worldWidth;
        camera.viewportHeight = worldHeight;
        camera.update();

        uiStage.getViewport().update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        messageStage.getViewport().update(width, height, true);
        if (imageStage != null) imageStage.getViewport().update(width, height, true);
        if (bookStage != null) bookStage.getViewport().update(width, height, true);

        uiScale = game.getUIScale();
        btnSize = game.getScaledSize(100);
        pauseSize = game.getScaledSize(70);
        screenW = TheFateGame.VIRTUAL_WIDTH;
        screenH = TheFateGame.VIRTUAL_HEIGHT;

        joystickBasePos = new Vector2(150 * uiScale, 150 * uiScale);
        joystickKnobPos = new Vector2(joystickBasePos.x, joystickBasePos.y);
        joystickBaseRadius = 70 * uiScale;
        joystickKnobRadius = 35 * uiScale;

        if (joystickBaseTexture != null) joystickBaseTexture.dispose();
        if (joystickKnobTexture != null) joystickKnobTexture.dispose();
        joystickBaseTexture = createJoystickBaseTexture();
        joystickKnobTexture = createJoystickKnobTexture();

        uiStage.clear();
        createUI();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(isPaused ? pauseStage : uiStage);
        game.startGameMusic();
    }

    @Override public void hide() {
        game.stopGameMusic();
    }

    @Override public void dispose() {
        uiStage.dispose();
        pauseStage.dispose();
        messageStage.dispose();
        if (imageStage != null) imageStage.dispose();
        if (bookStage != null) bookStage.dispose();
        if (currentImageTexture != null) currentImageTexture.dispose();
        if (currentNoteTexture != null) currentNoteTexture.dispose();
        if (joystickBaseTexture != null) joystickBaseTexture.dispose();
        if (joystickKnobTexture != null) joystickKnobTexture.dispose();
        if (world != null) world.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}