package com.example.wrenchempire.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wrenchempire.ui.theme.*

/**
 * High-detail Single-Screen Animated Garage Workshop.
 * Features 2 cars on lifts side-by-side (Red Sports Car & Blue Muscle Truck).
 * Much larger mechanic sprites (~50px tall) with clear, distinct comic animations:
 * - Fighting each other with comic dust clouds & clashing wrenches!
 * - Dancing with arms in air & musical notes!
 * - Drinking steaming coffee!
 * - Sleeping with big Zzz bubbles!
 * - Working with heavy sparks and smoke!
 */
@Composable
fun AnimatedWorkshopView(
    hiredWorkersCount: Int,
    modifier: Modifier = Modifier
) {
    val totalMechanics = hiredWorkersCount.coerceAtLeast(1)
    val hasSecondCar = totalMechanics >= 4

    val infiniteTransition = rememberInfiniteTransition(label = "unifiedWorkshopAnim")

    // Global cycle timer (6 seconds repeating cycle)
    val cyclePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globalCycle"
    )

    // Lift float
    val liftBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liftBounce"
    )

    // Fight cloud pulse & sparks
    val fightPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fightPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .border(2.dp, NesYellow)
            .background(GarageFloorDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ── 1. Brick Wall Background (upper 58%) ──
            drawRect(GarageWallTop, Offset(0f, 0f), Size(w, h * 0.58f))
            val brickH = 14f
            val brickW = 28f
            var by = 0f
            var brow = 0
            while (by < h * 0.58f) {
                val offX = if (brow % 2 == 0) 0f else brickW / 2
                var bx = -offX
                while (bx < w) {
                    drawRect(GarageWallBrick, Offset(bx + 1f, by + 1f), Size(brickW - 2f, brickH - 2f))
                    bx += brickW
                }
                by += brickH
                brow++
            }

            // ── 2. Checkered Workshop Floor (bottom 42%) ──
            val floorTop = h * 0.58f
            val tileSize = 22f
            var fy = floorTop
            var frow = 0
            while (fy < h) {
                var fx = 0f
                var fcol = 0
                while (fx < w) {
                    val tileColor = if ((frow + fcol) % 2 == 0) GarageFloorDark else GarageFloorTile
                    drawRect(tileColor, Offset(fx, fy), Size(tileSize, tileSize))
                    fx += tileSize
                    fcol++
                }
                fy += tileSize
                frow++
            }

            // Caution border
            drawLine(GarageLiftYellow, Offset(0f, floorTop), Offset(w, floorTop), strokeWidth = 3f)

            // ── 3. Central Tool Cabinet (Red Steel) ──
            val centerCabinetX = w * 0.50f - 18f
            drawBigToolCabinet(centerCabinetX, floorTop - 52f)

            // ── 4. Car 1: Left Bay (Red Sports Coupe on Lift) ──
            val lift1Center = w * 0.25f
            val car1Y = (h * 0.36f) + liftBounce
            drawHydraulicLift(lift1Center, h * 0.12f, floorTop, car1Y)
            drawBigSportsCar(lift1Center - 65f, car1Y - 42f)

            // ── 5. Car 2: Right Bay (Blue Muscle Truck on Lift) ──
            val lift2Center = w * 0.75f
            val car2Y = (h * 0.36f) + (liftBounce * 0.8f)
            drawHydraulicLift(lift2Center, h * 0.12f, floorTop, car2Y)
            if (hasSecondCar) {
                drawBigMuscleTruck(lift2Center - 68f, car2Y - 44f)
            } else {
                // Empty lift waiting for more crew
                drawEmptyLiftGhost(lift2Center - 45f, car2Y - 20f)
            }

            // ── 6. Big Mechanics Animated across the garage ──
            val activeToDraw = totalMechanics.coerceIn(1, 8)

            // Mechanic #1: Left Car Specialist (Works, Dances, Drinks Coffee)
            drawBigMechanic(
                x = lift1Center + 28f,
                floorY = floorTop,
                behavior = getMechanicBehavior(0, cyclePhase),
                sparkPulse = fightPulse
            )

            // Mechanic #2 & #3: FIGHT / KAVGA in the Center! (Hilarious bickering)
            if (activeToDraw >= 2) {
                val isFighting = (cyclePhase in 0.20f..0.70f)
                if (isFighting && activeToDraw >= 3) {
                    // Two mechanics clashing in the middle!
                    drawMechanicFight(
                        centerX = centerCabinetX + 18f,
                        floorY = floorTop,
                        pulse = fightPulse
                    )
                } else {
                    // Mechanic #2 working on Car 1 rear / sleeping
                    drawBigMechanic(
                        x = lift1Center - 75f,
                        floorY = floorTop,
                        behavior = getMechanicBehavior(1, cyclePhase),
                        sparkPulse = fightPulse
                    )
                }
            }

            // Mechanic #3 (if not fighting)
            if (activeToDraw >= 3 && (cyclePhase !in 0.20f..0.70f)) {
                drawBigMechanic(
                    x = centerCabinetX - 28f,
                    floorY = floorTop,
                    behavior = BigMechanicBehavior.COFFEE,
                    sparkPulse = fightPulse
                )
            }

            // Mechanics on Car 2 (Right Side)
            if (activeToDraw >= 4) {
                drawBigMechanic(
                    x = lift2Center + 26f,
                    floorY = floorTop,
                    behavior = getMechanicBehavior(2, cyclePhase),
                    sparkPulse = fightPulse
                )
            }

            if (activeToDraw >= 5) {
                drawBigMechanic(
                    x = lift2Center - 78f,
                    floorY = floorTop,
                    behavior = getMechanicBehavior(3, cyclePhase),
                    sparkPulse = fightPulse
                )
            }
        }

        // Top Status Header Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(NesNearBlack.copy(alpha = 0.9f))
                    .border(1.dp, NesYellow)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "🔧 DUAL-LIFT GARAGE 🔧",
                    style = MaterialTheme.typography.labelSmall,
                    color = NesYellow
                )
            }

            Box(
                modifier = Modifier
                    .background(NesNearBlack.copy(alpha = 0.9f))
                    .border(1.dp, NesGreen)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "🟢 $totalMechanics CREW ON DUTY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NesGreen
                )
            }
        }
    }
}

// ─── Mechanic Behavior Determination ─────────────────────────────────────────

private enum class BigMechanicBehavior {
    WORK, DANCE, SLEEP, COFFEE, CHEER
}

private fun getMechanicBehavior(slot: Int, phase: Float): BigMechanicBehavior {
    val localPhase = (phase + slot * 0.33f) % 1.0f
    return when {
        localPhase < 0.40f -> BigMechanicBehavior.WORK
        localPhase < 0.65f -> when (slot % 4) {
            0 -> BigMechanicBehavior.DANCE
            1 -> BigMechanicBehavior.SLEEP
            2 -> BigMechanicBehavior.COFFEE
            else -> BigMechanicBehavior.CHEER
        }
        else -> BigMechanicBehavior.WORK
    }
}

// ─── Drawing Components: Hydraulic Lift ─────────────────────────────────────

private fun DrawScope.drawHydraulicLift(centerX: Float, topY: Float, floorY: Float, platformY: Float) {
    val span = 145f
    val postL = centerX - span / 2
    val postR = centerX + span / 2

    // Steel Pillars
    drawRect(GarageLiftMetal, Offset(postL, topY), Size(12f, floorY - topY))
    drawRect(GarageLiftMetal, Offset(postR, topY), Size(12f, floorY - topY))

    // Hazard Stripes on Pillars
    drawLine(Color(0xFFFFD54F), Offset(postL, topY + 20f), Offset(postL + 12f, topY + 30f), strokeWidth = 2f)
    drawLine(Color(0xFFFFD54F), Offset(postR, topY + 20f), Offset(postR + 12f, topY + 30f), strokeWidth = 2f)

    // Platform
    drawRect(GarageLiftYellow, Offset(postL - 8f, platformY), Size(span + 28f, 9f))
}

private fun DrawScope.drawEmptyLiftGhost(x: Float, y: Float) {
    drawRect(
        color = Color(0x44FFFFFF),
        topLeft = Offset(x, y),
        size = Size(90f, 22f)
    )
}

// ─── Big Tool Cabinet ───────────────────────────────────────────────────────

private fun DrawScope.drawBigToolCabinet(x: Float, y: Float) {
    val red = Color(0xFFC62828)
    val darkRed = Color(0xFF8E0000)
    val drawerLine = Color(0xFF263238)
    val handle = Color(0xFFFFD54F)

    // Cabinet Main Body (36x50px)
    drawRect(red, Offset(x, y), Size(36f, 50f))
    drawRect(darkRed, Offset(x, y), Size(36f, 5f))

    // 4 Drawers
    var dy = y + 8f
    repeat(4) {
        drawRect(drawerLine, Offset(x + 3f, dy), Size(30f, 8f))
        drawRect(handle, Offset(x + 12f, dy + 2f), Size(12f, 3f))
        dy += 10f
    }
}

// ─── Big Car Models (~135px wide, ~50px tall) ───────────────────────────────

private fun DrawScope.drawBigSportsCar(x: Float, y: Float) {
    val red = Color(0xFFE53935)
    val darkRed = Color(0xFFB71C1C)
    val glass = Color(0xFF80D8FF)
    val tire = Color(0xFF212121)
    val rim = Color(0xFFEEEEEE)

    // Roof & Cabin
    drawRect(darkRed, Offset(x + 40f, y), Size(56f, 18f))
    drawRect(glass, Offset(x + 82f, y + 2f), Size(12f, 15f)) // front window
    drawRect(glass, Offset(x + 42f, y + 2f), Size(10f, 15f)) // rear window
    drawRect(glass, Offset(x + 54f, y + 2f), Size(26f, 15f)) // side window

    // Main Body
    drawRect(red, Offset(x + 10f, y + 18f), Size(116f, 20f))
    drawRect(red, Offset(x + 120f, y + 23f), Size(15f, 15f)) // front nose
    drawRect(darkRed, Offset(x, y + 20f), Size(12f, 18f)) // rear bumper

    // Lights
    drawRect(Color(0xFFFFEB3B), Offset(x + 132f, y + 25f), Size(5f, 7f))
    drawRect(Color(0xFFFF1744), Offset(x - 2f, y + 22f), Size(4f, 7f))

    // Big Wheels
    drawCircle(tire, radius = 12f, center = Offset(x + 32f, y + 38f))
    drawCircle(rim, radius = 6f, center = Offset(x + 32f, y + 38f))
    drawCircle(tire, radius = 12f, center = Offset(x + 110f, y + 38f))
    drawCircle(rim, radius = 6f, center = Offset(x + 110f, y + 38f))
}

private fun DrawScope.drawBigMuscleTruck(x: Float, y: Float) {
    val blue = Color(0xFF1E88E5)
    val darkBlue = Color(0xFF0D47A1)
    val glass = Color(0xFF80D8FF)
    val tire = Color(0xFF212121)
    val rim = Color(0xFFFFD54F)

    // Tall Truck Cab
    drawRect(darkBlue, Offset(x + 55f, y - 6f), Size(46f, 22f))
    drawRect(glass, Offset(x + 82f, y - 4f), Size(16f, 18f))
    drawRect(glass, Offset(x + 58f, y - 4f), Size(22f, 18f))

    // Flatbed & Body
    drawRect(darkBlue, Offset(x, y + 16f), Size(56f, 22f)) // rear flatbed
    drawRect(blue, Offset(x + 54f, y + 16f), Size(78f, 24f)) // front hood

    // Big Grill & Headlight
    drawRect(Color(0xFFEEEEEE), Offset(x + 128f, y + 22f), Size(6f, 16f))
    drawRect(Color(0xFFFFEB3B), Offset(x + 130f, y + 20f), Size(5f, 7f))

    // Huge Rugged Wheels
    drawCircle(tire, radius = 14f, center = Offset(x + 30f, y + 40f))
    drawCircle(rim, radius = 7f, center = Offset(x + 30f, y + 40f))
    drawCircle(tire, radius = 14f, center = Offset(x + 110f, y + 40f))
    drawCircle(rim, radius = 7f, center = Offset(x + 110f, y + 40f))
}

// ─── Big Mechanic Sprite (~50px tall, ~26px wide) ───────────────────────────

private fun DrawScope.drawBigMechanic(
    x: Float,
    floorY: Float,
    behavior: BigMechanicBehavior,
    sparkPulse: Float
) {
    val skin = Color(0xFFFFCC80)
    val cap = Color(0xFFD32F2F)
    val overalls = Color(0xFF1976D2)
    val boots = Color(0xFF3E2723)
    val wrench = Color(0xFFECEFF1)

    val y = floorY - 50f

    // 1. Cap & Head (Large!)
    drawRect(cap, Offset(x + 4f, y), Size(20f, 9f))
    drawRect(cap, Offset(x + 22f, y + 4f), Size(6f, 5f)) // visor
    drawRect(skin, Offset(x + 6f, y + 9f), Size(17f, 13f)) // face

    // Big Eyes
    if (behavior != BigMechanicBehavior.SLEEP) {
        drawRect(Color(0xFF212121), Offset(x + 18f, y + 13f), Size(3f, 4f))
    } else {
        // Sleeping eye line
        drawLine(Color(0xFF212121), Offset(x + 14f, y + 15f), Offset(x + 20f, y + 15f), strokeWidth = 2.5f)
    }

    // 2. Overalls (Torso)
    drawRect(overalls, Offset(x + 4f, y + 22f), Size(20f, 20f))
    drawRect(Color(0xFFE53935), Offset(x + 8f, y + 23f), Size(10f, 8f)) // shirt

    // 3. Legs & Boots
    if (behavior != BigMechanicBehavior.SLEEP) {
        drawRect(overalls, Offset(x + 5f, y + 40f), Size(7f, 10f))
        drawRect(overalls, Offset(x + 15f, y + 40f), Size(7f, 10f))
        drawRect(boots, Offset(x + 3f, y + 47f), Size(9f, 6f))
        drawRect(boots, Offset(x + 15f, y + 47f), Size(9f, 6f))
    } else {
        // Sitting sleeping on the floor
        drawRect(overalls, Offset(x, y + 36f), Size(24f, 9f))
        drawRect(boots, Offset(x + 18f, y + 41f), Size(10f, 6f))
    }

    // 4. Arms & Comic Props
    when (behavior) {
        BigMechanicBehavior.WORK -> {
            // Arm holding big wrench swinging at car
            drawRect(skin, Offset(x + 20f, y + 25f), Size(7f, 6f))
            drawLine(wrench, Offset(x + 25f, y + 26f), Offset(x + 36f, y + 17f), strokeWidth = 4f)
            // Big colorful sparks!
            drawCircle(Color(0xFFFFD600).copy(alpha = sparkPulse), radius = 4f, center = Offset(x + 38f, y + 15f))
            drawCircle(Color(0xFFFF6D00).copy(alpha = sparkPulse), radius = 3f, center = Offset(x + 44f, y + 10f))
            drawCircle(Color(0xFF00E5FF).copy(alpha = sparkPulse), radius = 2.5f, center = Offset(x + 34f, y + 8f))
        }

        BigMechanicBehavior.DANCE, BigMechanicBehavior.CHEER -> {
            // Both hands up dancing!
            drawRect(skin, Offset(x - 3f, y - 2f), Size(6f, 12f))
            drawRect(skin, Offset(x + 24f, y - 2f), Size(6f, 12f))
            // Giant musical notes ♪ ♫
            drawCircle(Color(0xFFFFD600), radius = 3f, center = Offset(x + 12f, y - 10f))
            drawLine(Color(0xFFFFD600), Offset(x + 14f, y - 10f), Offset(x + 14f, y - 20f), strokeWidth = 2.5f)
            drawCircle(Color(0xFF00E5FF), radius = 3f, center = Offset(x + 24f, y - 14f))
            drawLine(Color(0xFF00E5FF), Offset(x + 26f, y - 14f), Offset(x + 26f, y - 24f), strokeWidth = 2.5f)
        }

        BigMechanicBehavior.SLEEP -> {
            // Big comic Z z z bubbles!
            drawTextZzz(x + 24f, y - 12f)
        }

        BigMechanicBehavior.COFFEE -> {
            // Holding steaming coffee mug
            drawRect(skin, Offset(x + 18f, y + 26f), Size(6f, 6f))
            drawRect(Color(0xFFEEEEEE), Offset(x + 22f, y + 22f), Size(8f, 10f)) // mug
            drawRect(Color(0xFF795548), Offset(x + 23f, y + 23f), Size(6f, 4f)) // coffee
            // Steam curls
            drawLine(Color(0xBBFFFFFF), Offset(x + 25f, y + 19f), Offset(x + 27f, y + 14f), strokeWidth = 2f)
            drawLine(Color(0xBBFFFFFF), Offset(x + 28f, y + 19f), Offset(x + 26f, y + 12f), strokeWidth = 2f)
        }
    }
}

// ─── Comic Fight / Kavga Scene! ─────────────────────────────────────────────

private fun DrawScope.drawMechanicFight(centerX: Float, floorY: Float, pulse: Float) {
    val dustCloudColor = Color(0xDDFFFFFF)
    val starYellow = Color(0xFFFFD600)
    val starOrange = Color(0xFFFF3D00)
    val wrench = Color(0xFFECEFF1)

    val y = floorY - 35f

    // 1. Comic Fight Dust Cloud (Circles bursting)
    drawCircle(dustCloudColor, radius = 22f * pulse, center = Offset(centerX, y))
    drawCircle(dustCloudColor, radius = 17f, center = Offset(centerX - 14f, y + 4f))
    drawCircle(dustCloudColor, radius = 19f, center = Offset(centerX + 14f, y - 2f))

    // 2. Flying Wrenches clashing
    drawLine(wrench, Offset(centerX - 18f, y - 10f), Offset(centerX + 12f, y - 18f), strokeWidth = 4f)
    drawLine(wrench, Offset(centerX + 18f, y - 8f), Offset(centerX - 10f, y - 16f), strokeWidth = 4f)

    // 3. Comic Action Stars & Anger Veins 💢
    drawCircle(starYellow, radius = 5f, center = Offset(centerX + 16f, y - 22f))
    drawCircle(starOrange, radius = 4f, center = Offset(centerX - 20f, y - 20f))
    drawCircle(starYellow, radius = 3.5f, center = Offset(centerX, y - 26f))

    // Two mechanics peeking out of dust fighting!
    drawRect(Color(0xFFD32F2F), Offset(centerX - 28f, y + 2f), Size(12f, 10f)) // Left cap
    drawRect(Color(0xFF1976D2), Offset(centerX + 16f, y + 2f), Size(12f, 10f)) // Right cap
}

private fun DrawScope.drawTextZzz(x: Float, y: Float) {
    val zColor = Color(0xFF80D8FF)
    // Small Z
    drawRect(zColor, Offset(x, y + 8f), Size(6f, 2f))
    drawRect(zColor, Offset(x, y + 14f), Size(6f, 2f))
    drawLine(zColor, Offset(x + 5f, y + 9f), Offset(x + 1f, y + 13f), strokeWidth = 2f)

    // Big Z
    drawRect(zColor, Offset(x + 10f, y - 6f), Size(10f, 3f))
    drawRect(zColor, Offset(x + 10f, y + 4f), Size(10f, 3f))
    drawLine(zColor, Offset(x + 18f, y - 4f), Offset(x + 11f, y + 2f), strokeWidth = 3f)
}
