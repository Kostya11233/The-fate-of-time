package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.TheFateGame;
import com.mygdx.game.screens.StartMenuScreen;

public class Chapter3Screen implements Screen {
    private final TheFateGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Stage uiStage;
    private Stage dialogStage;
    private Stage choiceStage;
    private Stage messageStage;
    private Stage endGameStage;

    private Texture rocketTexture;
    private TextureRegion rocketRegion;
    private Texture meteoroidTexture;
    private TextureRegion meteoroidRegion;
    private Texture timeObjectTexture;
    private TextureRegion timeObjectRegion;
    private Texture spaceBackgroundTexture;
    private TextureRegion spaceBackgroundRegion;
    private Texture choiceBackgroundTexture;
    private TextureRegion choiceBackgroundRegion;
    private Texture dialogBgTexture;
    private TextureRegion dialogBgRegion;
    private Texture starTexture;
    private TextureRegion starRegion;

    private Vector2 rocketPosition;
    private float rocketSize = 100f;
    private boolean dragging = false;
    private Vector2 dragOffset = new Vector2();
    private float rocketRotation = 0f;
    private float targetRotation = 0f;

    private Array<Meteoroid> meteoroids;
    private float meteoroidSpawnTimer = 0f;
    private float meteoroidSpawnInterval = 0.8f;
    private int meteoroidsPassed = 0;
    private int requiredMeteoroidsToPass = 10;
    private float meteoroidSpeedMultiplier = 1f;

    private Array<Star> stars;
    private float starParallaxOffset = 0f;

    private Rectangle timeObjectBounds;
    private boolean timeObjectReached = false;
    private boolean showingDialog = false;
    private boolean showingChoice = false;
    private float timeObjectPulseScale = 1f;
    private float timeObjectPulseDir = 0.02f;

    private String[] alexDialog = {
            "Элис... Ты слышишь меня?",
            "Это твой отец. Я записал это сообщение на случай, если время остановится.",
            "Ты всегда была сильной. Я знал, что ты доберешься до временного объекта.",
            "Гиперкуб - это не просто машина времени. Это ключ к перезагрузке реальности.",
            "Но есть цена. Если ты используешь его... ты останешься в прошлом навсегда.",
            "Выбор за тобой, дочка. Я горжусь тобой, что бы ты ни решила."
    };
    private int dialogIndex = 0;
    private boolean dialogActive = false;

    private boolean isPaused = false;
    private float screenW, screenH;

    private float uiScale;
    private int btnSize;

    private float hitFlashAlpha = 0f;
    private float successPulseAlpha = 0f;
    private float timeObjectGlowAlpha = 0f;

    private Label progressLabel;

    private static final int ENDING_SACRIFICE = 1;
    private static final int ENDING_RETURN = 2;
    private static final int ENDING_CONTINUE = 3;

    private static class Meteoroid {
        float x, y, size;
        float velX, velY;
        float rotation;
        float rotationSpeed;
        Color color;

        Meteoroid(float x, float y, float size, float velX, float velY) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.velX = velX;
            this.velY = velY;
            this.rotation = MathUtils.random(0, 360);
            this.rotationSpeed = MathUtils.random(-180, 180);
            this.color = new Color(
                    0.6f + MathUtils.random(0.3f),
                    0.5f + MathUtils.random(0.3f),
                    0.4f + MathUtils.random(0.2f),
                    1f
            );
        }

        void update(float delta) {
            x += velX * delta;
            y += velY * delta;
            rotation += rotationSpeed * delta;
        }
    }

    private static class Star {
        float x, y, size, alpha, speed;
        Star(float x, float y, float size, float alpha, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.speed = speed;
        }
    }

    public Chapter3Screen(TheFateGame game) {
        this.game = game;
        this.batch = game.batch;
        this.camera = new OrthographicCamera();

        this.screenW = TheFateGame.VIRTUAL_WIDTH;
        this.screenH = TheFateGame.VIRTUAL_HEIGHT;
        this.uiScale = game.getUIScale();
        this.btnSize = game.getScaledSize(80);

        this.camera.setToOrtho(false, screenW, screenH);

        this.uiStage = new Stage(new ExtendViewport(screenW, screenH));
        this.dialogStage = new Stage(new ExtendViewport(screenW, screenH));
        this.choiceStage = new Stage(new ExtendViewport(screenW, screenH));
        this.messageStage = new Stage(new ExtendViewport(screenW, screenH));
        this.endGameStage = new Stage(new ExtendViewport(screenW, screenH));

        loadTextures();
        createStars();
        initGame();
        createUI();

        Gdx.input.setInputProcessor(uiStage);

        game.stopMenuMusic();
        game.stopGameMusic();
    }

    private void loadTextures() {
        try {
            rocketTexture = new Texture("racketa.jpg");
            rocketRegion = new TextureRegion(rocketTexture);
        } catch (Exception e) {
            rocketTexture = createFallbackTexture(0.8f, 0.3f, 0.3f);
            rocketRegion = new TextureRegion(rocketTexture);
        }

        try {
            meteoroidTexture = new Texture("mateorid.png");
            meteoroidRegion = new TextureRegion(meteoroidTexture);
        } catch (Exception e) {
            meteoroidTexture = createFallbackTexture(0.5f, 0.4f, 0.3f);
            meteoroidRegion = new TextureRegion(meteoroidTexture);
        }

        try {
            timeObjectTexture = new Texture("doshirak.png");
            timeObjectRegion = new TextureRegion(timeObjectTexture);
        } catch (Exception e) {
            timeObjectTexture = createFallbackTexture(0.2f, 0.8f, 0.2f);
            timeObjectRegion = new TextureRegion(timeObjectTexture);
        }

        try {
            spaceBackgroundTexture = new Texture("fon67.png");
            spaceBackgroundRegion = new TextureRegion(spaceBackgroundTexture);
        } catch (Exception e) {
            spaceBackgroundTexture = null;
        }

        try {
            choiceBackgroundTexture = new Texture("fon5.png");
            choiceBackgroundRegion = new TextureRegion(choiceBackgroundTexture);
        } catch (Exception e) {
            choiceBackgroundTexture = null;
        }

        try {
            dialogBgTexture = new Texture("button1.png");
            dialogBgRegion = new TextureRegion(dialogBgTexture);
        } catch (Exception e) {
            dialogBgTexture = createFallbackTexture(0.2f, 0.2f, 0.3f);
            dialogBgRegion = new TextureRegion(dialogBgTexture);
        }

        starTexture = createStarTexture();
        starRegion = new TextureRegion(starTexture);
    }

    private Texture createStarTexture() {
        Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fillCircle(1, 1, 1);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createFallbackTexture(float r, float g, float b) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void createStars() {
        stars = new Array<>();
        for (int i = 0; i < 200; i++) {
            float x = MathUtils.random(0, screenW);
            float y = MathUtils.random(0, screenH);
            float size = MathUtils.random(1, 3);
            float alpha = MathUtils.random(0.3f, 0.9f);
            float speed = MathUtils.random(20, 80);
            stars.add(new Star(x, y, size, alpha, speed));
        }
    }

    private void initGame() {
        rocketPosition = new Vector2(screenW / 2 - rocketSize / 2, 80);
        meteoroids = new Array<>();
        meteoroidSpawnTimer = 0f;
        meteoroidsPassed = 0;
        meteoroidSpeedMultiplier = 1f;
        timeObjectReached = false;
        showingDialog = false;
        showingChoice = false;
        dialogActive = false;
        isPaused = false;
        hitFlashAlpha = 0f;
        successPulseAlpha = 0f;
        timeObjectGlowAlpha = 0f;

        float objSize = 130f;
        timeObjectBounds = new Rectangle(screenW / 2 - objSize / 2, screenH - 200, objSize, objSize);
    }

    private void createUI() {
        uiStage.clear();

        Texture pauseTex = createButtonTexture(0.8f, 0.8f, 0.2f);
        ImageButton pauseBtn = new ImageButton(new TextureRegionDrawable(pauseTex));
        int pauseSize = game.getScaledSize(60);
        pauseBtn.setSize(pauseSize, pauseSize);
        pauseBtn.setPosition(screenW - pauseSize - 20, screenH - pauseSize - 20);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (!showingDialog && !showingChoice && !timeObjectReached) {
                    isPaused = true;
                    createPauseDialog();
                }
            }
        });
        uiStage.addActor(pauseBtn);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.titleFont;
        labelStyle.fontColor = Color.GOLD;

        progressLabel = new Label(meteoroidsPassed + "/" + requiredMeteoroidsToPass, labelStyle);
        progressLabel.setPosition(screenW / 2 - 50, screenH - 80);
        progressLabel.setFontScale(1.8f);
        uiStage.addActor(progressLabel);

        Label.LabelStyle smallStyle = new Label.LabelStyle();
        smallStyle.font = game.smallFont;
        smallStyle.fontColor = Color.CYAN;

        Label progressPercentLabel = new Label("УКЛОНЕНИЙ", smallStyle);
        progressPercentLabel.setPosition(screenW / 2 - 70, screenH - 120);
        uiStage.addActor(progressPercentLabel);

        Label hintLabel = new Label("← ПЕРЕМЕЩАЙ РАКЕТУ ПАЛЬЦЕМ →", smallStyle);
        hintLabel.setPosition(screenW / 2 - 180, 30);
        hintLabel.setFontScale(0.9f);
        uiStage.addActor(hintLabel);
    }

    private Texture createButtonTexture(float r, float g, float b) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 0.8f);
        pixmap.fillCircle(32, 32, 30);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void createPauseDialog() {
        dialogStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.85f);
        dialogStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        dialogStage.addActor(table);

        Table panel = new Table();
        if (dialogBgRegion != null) panel.setBackground(new TextureRegionDrawable(dialogBgRegion));
        panel.pad(40);

        Label title = new Label("ПАУЗА", new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = Color.GOLD;
        }});
        title.setFontScale(1.5f);

        TextButton continueBtn = new TextButton("ПРОДОЛЖИТЬ", new TextButton.TextButtonStyle() {{ font = game.font; }});
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                isPaused = false;
                dialogStage.clear();
                Gdx.input.setInputProcessor(uiStage);
            }
        });

        TextButton exitBtn = new TextButton("В МЕНЮ", new TextButton.TextButtonStyle() {{ font = game.font; }});
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new StartMenuScreen(game));
            }
        });

        panel.add(title).padBottom(40).row();
        panel.add(continueBtn).width(250).height(65).padBottom(20).row();
        panel.add(exitBtn).width(250).height(65).row();
        table.add(panel).center();

        Gdx.input.setInputProcessor(dialogStage);
    }

    private void showMessage(String msg, float duration, Color color) {
        final Stage tempStage = new Stage(new ExtendViewport(screenW, screenH));
        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.7f);
        tempStage.addActor(darkBg);
        Table table = new Table();
        table.setFillParent(true);
        tempStage.addActor(table);

        Label label = new Label(msg, new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = color;
        }});
        label.setFontScale(1.2f);
        table.add(label).center();

        Gdx.input.setInputProcessor(tempStage);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                tempStage.dispose();
                Gdx.input.setInputProcessor(uiStage);
            }
        }, duration);
    }

    private void spawnMeteoroid() {
        float size = 55 + MathUtils.random(35);
        float x = MathUtils.random(-size * 0.5f, screenW - size * 0.5f);
        float y = screenH + size;

        float speedY = -MathUtils.random(180f, 400f) * meteoroidSpeedMultiplier;
        float speedX = MathUtils.random(-120f, 120f);

        meteoroids.add(new Meteoroid(x, y, size, speedX, speedY));

        meteoroidSpeedMultiplier = Math.min(1.8f, meteoroidSpeedMultiplier + 0.003f);
    }

    private void updateMeteoroids(float delta) {
        for (int i = meteoroids.size - 1; i >= 0; i--) {
            Meteoroid m = meteoroids.get(i);
            m.update(delta);

            Rectangle rocketRect = new Rectangle(rocketPosition.x, rocketPosition.y, rocketSize, rocketSize);
            Rectangle meteorRect = new Rectangle(m.x, m.y, m.size, m.size);

            if (rocketRect.overlaps(meteorRect)) {
                meteoroids.removeIndex(i);
                hitFlashAlpha = 0.8f;
                meteoroidsPassed = Math.max(0, meteoroidsPassed - 1);
                updateProgressDisplay();
                showMessage("-1 УКЛОНЕНИЕ", 0.8f, Color.RED);
                continue;
            }

            if (m.y + m.size < 0) {
                meteoroids.removeIndex(i);
                meteoroidsPassed++;
                updateProgressDisplay();
                successPulseAlpha = 0.5f;

                if (meteoroidsPassed >= requiredMeteoroidsToPass && !timeObjectReached && !dialogActive) {
                    timeObjectReached = true;
                    showMessage("ВРЕМЕННОЙ ОБЪЕКТ ДОСТИГНУТ!", 1.5f, Color.GOLD);
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            startAlexDialog();
                        }
                    }, 1.5f);
                }
            }
        }

        if (!timeObjectReached && !dialogActive && !showingChoice) {
            meteoroidSpawnTimer += delta;
            float currentInterval = Math.max(0.35f, meteoroidSpawnInterval - (meteoroidsPassed * 0.02f));
            while (meteoroidSpawnTimer >= currentInterval) {
                meteoroidSpawnTimer -= currentInterval;
                spawnMeteoroid();
            }
        }
    }

    private void updateProgressDisplay() {
        progressLabel.setText(meteoroidsPassed + "/" + requiredMeteoroidsToPass);

        if (meteoroidsPassed >= requiredMeteoroidsToPass) {
            progressLabel.setColor(Color.GREEN);
        } else if (meteoroidsPassed >= requiredMeteoroidsToPass * 0.7f) {
            progressLabel.setColor(Color.YELLOW);
        } else {
            progressLabel.setColor(Color.WHITE);
        }
    }

    private void startAlexDialog() {
        dialogActive = true;
        showingDialog = true;
        dialogIndex = 0;
        isPaused = true;
        showDialogLine();
    }

    private void showDialogLine() {
        dialogStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 0.85f);
        dialogStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        dialogStage.addActor(table);

        Table dialogBox = new Table();
        if (dialogBgRegion != null) {
            dialogBox.setBackground(new TextureRegionDrawable(dialogBgRegion));
        }
        dialogBox.pad(30);

        Label nameLabel = new Label("✧ АЛЕКС (ОТЕЦ) ✧", new Label.LabelStyle() {{
            font = game.font;
            fontColor = Color.GOLD;
        }});
        nameLabel.setFontScale(1.2f);

        Label textLabel = new Label(alexDialog[dialogIndex], new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = Color.WHITE;
        }});
        textLabel.setWrap(true);

        TextButton nextBtn = new TextButton("ДАЛЕЕ →", new TextButton.TextButtonStyle() {{
            font = game.font;
        }});
        nextBtn.getLabel().setFontScale(0.9f);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialogIndex++;
                if (dialogIndex >= alexDialog.length) {
                    closeDialogAndShowChoice();
                } else {
                    showDialogLine();
                }
            }
        });

        dialogBox.add(nameLabel).padBottom(15).row();
        dialogBox.add(textLabel).width(550).padBottom(25).row();
        dialogBox.add(nextBtn).width(120).height(50);

        table.add(dialogBox).center().bottom().padBottom(100);

        Gdx.input.setInputProcessor(dialogStage);
    }

    private void closeDialogAndShowChoice() {
        showingDialog = false;
        dialogActive = false;
        dialogStage.clear();
        isPaused = false;
        showChoiceDialog();
    }

    private void showChoiceDialog() {
        showingChoice = true;
        choiceStage.clear();

        if (choiceBackgroundRegion != null) {
            Image bgImage = new Image(choiceBackgroundRegion);
            bgImage.setFillParent(true);
            choiceStage.addActor(bgImage);
        } else {
            Table bgTable = new Table();
            bgTable.setFillParent(true);
            bgTable.setColor(0.05f, 0.05f, 0.15f, 1);
            choiceStage.addActor(bgTable);
        }

        Table darkOverlay = new Table();
        darkOverlay.setFillParent(true);
        darkOverlay.setColor(0, 0, 0, 0.5f);
        choiceStage.addActor(darkOverlay);

        Table table = new Table();
        table.setFillParent(true);
        choiceStage.addActor(table);

        Table panel = new Table();
        if (dialogBgRegion != null) {
            panel.setBackground(new TextureRegionDrawable(dialogBgRegion));
        }
        panel.pad(40);

        Label title = new Label("ВЫБОР СУДЬБЫ", new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = Color.GOLD;
        }});
        title.setFontScale(1.8f);

        Label question = new Label("Что сделает Элис с Гиперкубом?", new Label.LabelStyle() {{
            font = game.font;
            fontColor = Color.WHITE;
        }});
        question.setFontScale(1.2f);

        TextButton btn1 = new TextButton("", new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        Table btn1Table = new Table();
        Label btn1Title = new Label("🌌 ОСТАТЬСЯ В ПРОШЛОМ", new Label.LabelStyle() {{ font = game.font; fontColor = Color.ORANGE; }});
        btn1Title.setFontScale(1.2f);
        Label btn1Sub = new Label("Спасти отца, но исчезнуть из настоящего", new Label.LabelStyle() {{ font = game.smallFont; fontColor = Color.LIGHT_GRAY; }});
        btn1Sub.setFontScale(0.8f);
        btn1Table.add(btn1Title).padBottom(5).row();
        btn1Table.add(btn1Sub).row();
        btn1.add(btn1Table).pad(12);
        btn1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showEnding(ENDING_SACRIFICE);
            }
        });

        TextButton btn2 = new TextButton("", new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        Table btn2Table = new Table();
        Label btn2Title = new Label("✨ ВЕРНУТЬСЯ В НАСТОЯЩЕЕ", new Label.LabelStyle() {{ font = game.font; fontColor = Color.GREEN; }});
        btn2Title.setFontScale(1.2f);
        Label btn2Sub = new Label("Отец останется в прошлом, мир спасён", new Label.LabelStyle() {{ font = game.smallFont; fontColor = Color.LIGHT_GRAY; }});
        btn2Sub.setFontScale(0.8f);
        btn2Table.add(btn2Title).padBottom(5).row();
        btn2Table.add(btn2Sub).row();
        btn2.add(btn2Table).pad(12);
        btn2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showEnding(ENDING_RETURN);
            }
        });

        TextButton btn3 = new TextButton("", new TextButton.TextButtonStyle() {{ font = game.smallFont; }});
        Table btn3Table = new Table();
        Label btn3Title = new Label("⏰ ЗАМОРОЗИТЬ ВРЕМЯ", new Label.LabelStyle() {{ font = game.font; fontColor = Color.CYAN; }});
        btn3Title.setFontScale(1.2f);
        Label btn3Sub = new Label("Остановить момент выбора навсегда", new Label.LabelStyle() {{ font = game.smallFont; fontColor = Color.LIGHT_GRAY; }});
        btn3Sub.setFontScale(0.8f);
        btn3Table.add(btn3Title).padBottom(5).row();
        btn3Table.add(btn3Sub).row();
        btn3.add(btn3Table).pad(12);
        btn3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showEnding(ENDING_CONTINUE);
            }
        });

        panel.add(title).padBottom(20).row();
        panel.add(question).padBottom(40).row();
        panel.add(btn1).width(420).height(90).padBottom(15).row();
        panel.add(btn2).width(420).height(90).padBottom(15).row();
        panel.add(btn3).width(420).height(90).row();

        table.add(panel).center();
        Gdx.input.setInputProcessor(choiceStage);
    }

    private void showEnding(int endingType) {
        showingChoice = false;
        choiceStage.clear();

        String endingTitle = "";
        String endingText = "";
        Color titleColor = Color.GOLD;

        switch (endingType) {
            case ENDING_SACRIFICE:
                endingTitle = "КОНЦОВКА: ЖЕРТВА";
                endingText = "Элис активирует Гиперкуб и переносится в прошлое.\n\n" +
                        "Она успевает спасти отца, но сама исчезает из реальности.\n" +
                        "Алекс живёт и помнит свою дочь.\n\n" +
                        "Временная петля замкнулась.\n\n" +
                        "★ СПАСИБО ЗА ИГРУ! ★";
                titleColor = Color.ORANGE;
                break;
            case ENDING_RETURN:
                endingTitle = "КОНЦОВКА: ВОЗВРАЩЕНИЕ";
                endingText = "Элис возвращается в настоящее, забрав частицу Гиперкуба.\n\n" +
                        "Отец остался в прошлом, но мир спасён от временной аномалии.\n" +
                        "Элис продолжает своё путешествие сквозь время...\n\n" +
                        "★ СПАСИБО ЗА ИГРУ! ★";
                titleColor = Color.GREEN;
                break;
            case ENDING_CONTINUE:
                endingTitle = "КОНЦОВКА: ЗАМОРОЗКА";
                endingText = "Элис замораживает время в момент выбора.\n\n" +
                        "Никто не знает, что произошло дальше.\n" +
                        "Может быть, когда-нибудь время снова пойдёт...\n\n" +
                        "★ СПАСИБО ЗА ИГРУ! ★";
                titleColor = Color.CYAN;
                break;
        }

        game.prefs.putBoolean("chapter3_completed", true);
        game.prefs.putInteger("saved_chapter", 3);
        game.prefs.flush();

        endGameStage.clear();

        Table darkBg = new Table();
        darkBg.setFillParent(true);
        darkBg.setColor(0, 0, 0, 1);
        endGameStage.addActor(darkBg);

        Table table = new Table();
        table.setFillParent(true);
        endGameStage.addActor(table);

        Table panel = new Table();
        if (dialogBgRegion != null) panel.setBackground(new TextureRegionDrawable(dialogBgRegion));
        panel.pad(50);

        Color finalTitleColor = titleColor;
        Label titleLabel = new Label(endingTitle, new Label.LabelStyle() {{
            font = game.titleFont;
            fontColor = finalTitleColor;
        }});
        titleLabel.setFontScale(1.6f);

        Label textLabel = new Label(endingText, new Label.LabelStyle() {{
            font = game.smallFont;
            fontColor = Color.WHITE;
        }});
        textLabel.setWrap(true);
        textLabel.setAlignment(1);

        TextButton menuBtn = new TextButton("В ГЛАВНОЕ МЕНЮ", new TextButton.TextButtonStyle() {{ font = game.font; }});
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.stopGameMusic();
                game.startMenuMusic();
                game.setScreen(new StartMenuScreen(game));
            }
        });

        TextButton exitBtn = new TextButton("ВЫЙТИ ИЗ ИГРЫ", new TextButton.TextButtonStyle() {{ font = game.font; }});
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table btnTable = new Table();
        btnTable.add(menuBtn).width(220).height(60).padRight(20);
        btnTable.add(exitBtn).width(220).height(60);

        panel.add(titleLabel).padBottom(30).row();
        panel.add(textLabel).width(550).padBottom(50).row();
        panel.add(btnTable).row();

        table.add(panel).center();
        Gdx.input.setInputProcessor(endGameStage);
    }

    private void updatePlayerDrag() {
        if (isPaused || showingDialog || showingChoice || timeObjectReached) return;

        if (Gdx.input.isTouched()) {
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            Vector3 touchPos = new Vector3(screenX, screenY, 0);
            uiStage.getViewport().unproject(touchPos);

            float newX = touchPos.x - rocketSize / 2;
            float newY = touchPos.y - rocketSize / 2;

            newX = Math.max(5, Math.min(newX, screenW - rocketSize - 5));
            newY = Math.max(30, Math.min(newY, screenH - rocketSize - 150));

            rocketPosition.set(newX, newY);

            targetRotation = (newX - (screenW - rocketSize) / 2) * 0.3f;
            targetRotation = Math.min(25, Math.max(-25, targetRotation));
            rocketRotation += (targetRotation - rocketRotation) * 0.15f;
        } else {
            targetRotation = 0;
            rocketRotation += (targetRotation - rocketRotation) * 0.1f;
        }
    }

    private void updateVisualEffects(float delta) {
        hitFlashAlpha = Math.max(0, hitFlashAlpha - delta * 4f);
        successPulseAlpha = Math.max(0, successPulseAlpha - delta * 2f);

        if (timeObjectReached) {
            timeObjectGlowAlpha = 0.5f + (float) Math.sin(System.currentTimeMillis() * 0.005) * 0.3f;
        } else {
            timeObjectPulseScale += timeObjectPulseDir;
            if (timeObjectPulseScale >= 1.15f) {
                timeObjectPulseScale = 1.15f;
                timeObjectPulseDir = -0.008f;
            } else if (timeObjectPulseScale <= 0.85f) {
                timeObjectPulseScale = 0.85f;
                timeObjectPulseDir = 0.008f;
            }
        }

        starParallaxOffset += delta * 15f;
        if (starParallaxOffset > screenW) starParallaxOffset -= screenW;
    }

    private void drawBackground() {
        if (spaceBackgroundRegion != null) {
            batch.draw(spaceBackgroundRegion, 0, 0, screenW, screenH);
        } else {
            batch.setColor(0.02f, 0.02f, 0.08f, 1);
            Texture fallback = createFallbackTexture(0.02f, 0.02f, 0.08f);
            batch.draw(fallback, 0, 0, screenW, screenH);
            batch.setColor(1, 1, 1, 1);
        }

        for (Star s : stars) {
            batch.setColor(1, 1, 1, s.alpha);
            float parallaxX = s.x + starParallaxOffset * (s.speed / 100f);
            if (parallaxX > screenW) parallaxX -= screenW;
            if (parallaxX < -s.size) parallaxX += screenW;
            batch.draw(starRegion, parallaxX, s.y, s.size, s.size);
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void drawMeteoroids() {
        for (Meteoroid m : meteoroids) {
            batch.setColor(m.color.r, m.color.g, m.color.b, m.color.a);
            batch.draw(meteoroidRegion, m.x, m.y, m.size / 2, m.size / 2, m.size, m.size, 1, 1, m.rotation);
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void drawTimeObject() {
        if (timeObjectRegion != null) {
            float objSize = timeObjectBounds.width;
            float objX = timeObjectBounds.x;
            float objY = timeObjectBounds.y;

            if (timeObjectGlowAlpha > 0 || !timeObjectReached) {
                batch.setColor(1, 0.8f, 0.2f, 0.3f * (timeObjectReached ? timeObjectGlowAlpha : 1f));
                batch.draw(timeObjectRegion, objX - 15, objY - 15, objSize + 30, objSize + 30);
            }

            if (!timeObjectReached) {
                float scale = timeObjectPulseScale;
                float offset = (objSize * scale - objSize) / 2;
                batch.setColor(1, 1, 1, 1);
                batch.draw(timeObjectRegion, objX - offset, objY - offset, objSize * scale, objSize * scale);
            } else {
                batch.setColor(1, 1, 1, 1);
                batch.draw(timeObjectRegion, objX, objY, objSize, objSize);
            }

            batch.setColor(1, 1, 1, 1);
        }
    }

    private void drawRocket() {
        batch.draw(rocketRegion,
                rocketPosition.x + rocketSize / 2, rocketPosition.y + rocketSize / 2,
                rocketSize / 2, rocketSize / 2,
                rocketSize, rocketSize,
                1, 1,
                rocketRotation);
    }

    private void drawVisualEffects() {
        if (hitFlashAlpha > 0) {
            batch.setColor(1, 0, 0, hitFlashAlpha * 0.4f);
            Texture fallback = createFallbackTexture(1, 0, 0);
            batch.draw(fallback, 0, 0, screenW, screenH);
        }

        if (successPulseAlpha > 0) {
            batch.setColor(0, 1, 0, successPulseAlpha * 0.3f);
            Texture fallback = createFallbackTexture(0, 1, 0);
            batch.draw(fallback, 0, 0, screenW, screenH);
        }

        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void render(float delta) {
        if (!isPaused && !showingDialog && !showingChoice && !timeObjectReached) {
            updatePlayerDrag();
            updateMeteoroids(delta);
            updateVisualEffects(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawBackground();
        drawTimeObject();
        drawMeteoroids();
        drawRocket();
        drawVisualEffects();

        batch.end();

        uiStage.act(delta);
        uiStage.draw();

        if (showingDialog) {
            dialogStage.act(delta);
            dialogStage.draw();
        }

        if (showingChoice) {
            choiceStage.act(delta);
            choiceStage.draw();
        }

        if (endGameStage.getActors().size > 0) {
            endGameStage.act(delta);
            endGameStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        camera.setToOrtho(false, screenW, screenH);
        uiStage.getViewport().update(width, height, true);
        dialogStage.getViewport().update(width, height, true);
        choiceStage.getViewport().update(width, height, true);
        endGameStage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        uiStage.dispose();
        dialogStage.dispose();
        choiceStage.dispose();
        endGameStage.dispose();

        if (rocketTexture != null) rocketTexture.dispose();
        if (meteoroidTexture != null) meteoroidTexture.dispose();
        if (timeObjectTexture != null) timeObjectTexture.dispose();
        if (spaceBackgroundTexture != null) spaceBackgroundTexture.dispose();
        if (choiceBackgroundTexture != null) choiceBackgroundTexture.dispose();
        if (dialogBgTexture != null) dialogBgTexture.dispose();
        if (starTexture != null) starTexture.dispose();
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
}