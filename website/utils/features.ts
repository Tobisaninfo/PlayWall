import featuresData from '../../doc/features.json'

// doc/features.json ist die einzige Quelle für die Feature-Cluster (PW-203).
// Neue Cluster oder Sprachtexte werden dort gepflegt, nicht hier dupliziert.

export interface FeatureClusterContent {
    title: string
    description: string
    features: string[]
}

export interface FeatureCluster {
    id: string
    screenshot: string
    de: FeatureClusterContent
    en: FeatureClusterContent
}

export type FeatureLocale = 'de' | 'en'

export const featuresProduct: string = featuresData.product
export const featureClusters: FeatureCluster[] = featuresData.clusters as FeatureCluster[]

export function localizedFeatureClusters(locale: FeatureLocale) {
    return featureClusters.map((cluster) => ({
        id: cluster.id,
        screenshot: cluster.screenshot,
        ...cluster[locale],
    }))
}

// Intrinsische Bildmaße der Screenshots aus doc/screenshots (ausgeliefert
// über nitro.publicAssets, siehe nuxt.config.ts) — hier statt in
// features.json, da es sich um ein Präsentationsdetail handelt, nicht um
// Feature-Inhalt. Muss von Hand gepflegt werden, wenn ein Cluster auf ein
// neues Bild zeigt.
export const screenshotDimensions: Record<string, [number, number]> = {
    'project-management.png': [1000, 1176],
    'project-settings.png': [1700, 1060],
    'page-settings.png': [1700, 1000],
    'pad-grid.png': [1680, 1152],
    'pad-drag.png': [1680, 1152],
    'pad-settings.png': [2080, 1160],
    'fading.png': [2080, 1160],
    'mapping.png': [2080, 1460],
    'missing-media.png': [2800, 1118],
}
