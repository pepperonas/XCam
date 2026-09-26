import math, json, sys
C=54.0
def cookie(n, rmean, amp, steps=None, phase=-math.pi/2):
    # smooth "cookie" (MaterialShapes.CookieNSided-like): radius modulated by cos(n*theta)
    steps = steps or n*6
    pts=[]
    for i in range(steps):
        t = phase + 2*math.pi*i/steps
        r = rmean + amp*math.cos(n*(t-phase))
        pts.append((C+r*math.cos(t), C+r*math.sin(t)))
    # Catmull-Rom -> cubic bezier for smoothness
    d=f"M{pts[0][0]:.2f},{pts[0][1]:.2f}"
    N=len(pts)
    for i in range(N):
        p0=pts[(i-1)%N]; p1=pts[i]; p2=pts[(i+1)%N]; p3=pts[(i+2)%N]
        c1=(p1[0]+(p2[0]-p0[0])/6, p1[1]+(p2[1]-p0[1])/6)
        c2=(p2[0]-(p3[0]-p1[0])/6, p2[1]-(p3[1]-p1[1])/6)
        d+=f"C{c1[0]:.2f},{c1[1]:.2f} {c2[0]:.2f},{c2[1]:.2f} {p2[0]:.2f},{p2[1]:.2f}"
    return d+"Z"
def circle(r, cx=C, cy=C):
    return (f"M{cx-r:.2f},{cy:.2f}A{r:.2f},{r:.2f} 0 1,1 {cx+r:.2f},{cy:.2f}"
            f"A{r:.2f},{r:.2f} 0 1,1 {cx-r:.2f},{cy:.2f}Z")
if __name__=='__main__':
    n,rm,amp,hole,dot = int(sys.argv[1]),float(sys.argv[2]),float(sys.argv[3]),float(sys.argv[4]),float(sys.argv[5])
    print(cookie(n,rm,amp)+circle(hole)+circle(dot))

def rrect(x, y, w, h, r):
    return (f"M{x+r:.2f},{y:.2f}H{x+w-r:.2f}A{r:.2f},{r:.2f} 0 0,1 {x+w:.2f},{y+r:.2f}V{y+h-r:.2f}"
            f"A{r:.2f},{r:.2f} 0 0,1 {x+w-r:.2f},{y+h:.2f}H{x+r:.2f}A{r:.2f},{r:.2f} 0 0,1 {x:.2f},{y+h-r:.2f}"
            f"V{y+r:.2f}A{r:.2f},{r:.2f} 0 0,1 {x+r:.2f},{y:.2f}Z")

def lens_tab(x, y, w, h, r):
    """The camera's lens 'wedge': a trapezoid opening to the right, corners softened."""
    # simple rounded trapezoid via polygon with small corner arcs approximated by quads
    x0, x1 = x, x + w
    yt0, yb0 = y + h*0.30, y + h*0.70   # narrow side (joins the body)
    yt1, yb1 = y, y + h                 # wide side
    return (f"M{x0:.2f},{yt0+r:.2f}Q{x0:.2f},{yt0:.2f} {x0+r:.2f},{yt0-r*0.45:.2f}"
            f"L{x1-r:.2f},{yt1+r*0.45:.2f}Q{x1:.2f},{yt1:.2f} {x1:.2f},{yt1+r:.2f}"
            f"V{yb1-r:.2f}Q{x1:.2f},{yb1:.2f} {x1-r:.2f},{yb1-r*0.45:.2f}"
            f"L{x0+r:.2f},{yb0+r*0.45:.2f}Q{x0:.2f},{yb0:.2f} {x0:.2f},{yb0-r:.2f}Z")

def camera(dot=True, k=1.0):
    # body + lens wedge, centred on (54,54), inside a ~30 unit wide box
    bw, bh = 25.0*k, 20.0*k
    tw, th = 9.0*k, 16.0*k
    gap = 1.6*k
    total = bw + gap + tw
    bx = C - total/2
    by = C - bh/2
    body = rrect(bx, by, bw, bh, 5.0*k)
    tab = lens_tab(bx + bw + gap, C - th/2, tw, th, 2.2*k)
    d = body + tab
    if dot:
        d += circle(3.6*k, bx + 7.0*k, by + 7.0*k)
    return d
