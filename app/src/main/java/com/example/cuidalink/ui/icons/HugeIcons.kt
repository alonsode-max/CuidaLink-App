package com.example.cuidalink.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Subconjunto del pack gratuito Hugeicons (stroke rounded) usado en la app. */
object HugeIcons {
    /** Hugeicons "Home01" (stroke rounded). */
    val Home: ImageVector by lazy {
        hugeIcon(
            name = "Home",
            paths = listOf(
                HugePath("M3 11.9896V14.5C3 17.7998 3 19.4497 4.02513 20.4749C5.05025 21.5 6.70017 21.5 10 21.5H14C17.2998 21.5 18.9497 21.5 19.9749 20.4749C21 19.4497 21 17.7998 21 14.5V11.9896C21 10.3083 21 9.46773 20.6441 8.74005C20.2882 8.01237 19.6247 7.49628 18.2976 6.46411L16.2976 4.90855C14.2331 3.30285 13.2009 2.5 12 2.5C10.7991 2.5 9.76689 3.30285 7.70242 4.90855L5.70241 6.46411C4.37533 7.49628 3.71179 8.01237 3.3559 8.74005C3 9.46773 3 10.3083 3 11.9896Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M15.0002 17C14.2007 17.6224 13.1504 18 12.0002 18C10.8499 18 9.79971 17.6224 9.00018 17", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Brain02" (stroke rounded). */
    val Brain: ImageVector by lazy {
        hugeIcon(
            name = "Brain",
            paths = listOf(
                HugePath("M4.22222 21.9948V18.4451C4.22222 17.1737 3.88927 16.5128 3.23482 15.4078C2.4503 14.0833 2 12.5375 2 10.8866C2 5.97866 5.97969 2 10.8889 2C15.7981 2 19.7778 5.97866 19.7778 10.8866C19.7778 11.4663 19.7778 11.7562 19.802 11.9187C19.8598 12.3072 20.0411 12.6414 20.2194 12.9873L22 16.4407L20.6006 17.1402C20.195 17.3429 19.9923 17.4443 19.851 17.6314C19.7097 17.8184 19.67 18.0296 19.5904 18.4519L19.5826 18.4931C19.4004 19.4606 19.1993 20.5286 18.6329 21.2024C18.4329 21.4403 18.1853 21.6336 17.9059 21.7699C17.4447 21.9948 16.8777 21.9948 15.7437 21.9948C15.219 21.9948 14.6928 22.0069 14.1682 21.9942C12.9247 21.9639 12 20.9184 12 19.7044", isFill = false, strokeWidth = 1.5f),
                HugePath("M14.388 10.5315C13.9617 10.5315 13.5729 10.3702 13.2784 10.1048M14.388 10.5315C14.388 11.6774 13.7241 12.7658 12.4461 12.7658C11.1681 12.7658 10.5043 13.8541 10.5043 15M14.388 10.5315C16.5373 10.5315 16.5373 7.18017 14.388 7.18017C14.1927 7.18017 14.0053 7.21403 13.8312 7.27624C13.9362 4.77819 10.3349 4.1 9.51923 6.44018M10.5043 8.29729C10.5043 7.52323 10.1133 6.8411 9.51923 6.44018M9.51923 6.44018C7.66742 5.19034 5.19883 7.4331 6.37324 9.43277C4.40226 9.72827 4.61299 12.7658 6.6205 12.7658C7.18344 12.7658 7.68111 12.4844 7.98234 12.0538", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Calendar03" (stroke rounded). */
    val Calendar: ImageVector by lazy {
        hugeIcon(
            name = "Calendar",
            paths = listOf(
                HugePath("M16 2V6M8 2V6", isFill = false, strokeWidth = 1.5f),
                HugePath("M13 4H11C7.22876 4 5.34315 4 4.17157 5.17157C3 6.34315 3 8.22876 3 12V14C3 17.7712 3 19.6569 4.17157 20.8284C5.34315 22 7.22876 22 11 22H13C16.7712 22 18.6569 22 19.8284 20.8284C21 19.6569 21 17.7712 21 14V12C21 8.22876 21 6.34315 19.8284 5.17157C18.6569 4 16.7712 4 13 4Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M3 10H21", isFill = false, strokeWidth = 1.5f),
                HugePath("M12.1258 14H12.0008M12.1258 18H12.0008M7.625 14H7.5M7.625 18H7.5M16.625 14H16.5M12.2508 14C12.2508 14.1381 12.1389 14.25 12.0008 14.25C11.8628 14.25 11.7508 14.1381 11.7508 14C11.7508 13.8619 11.8628 13.75 12.0008 13.75C12.1389 13.75 12.2508 13.8619 12.2508 14ZM12.2508 18C12.2508 18.1381 12.1389 18.25 12.0008 18.25C11.8628 18.25 11.7508 18.1381 11.7508 18C11.7508 17.8619 11.8628 17.75 12.0008 17.75C12.1389 17.75 12.2508 17.8619 12.2508 18ZM7.75 14C7.75 14.1381 7.63807 14.25 7.5 14.25C7.36193 14.25 7.25 14.1381 7.25 14C7.25 13.8619 7.36193 13.75 7.5 13.75C7.63807 13.75 7.75 13.8619 7.75 14ZM7.75 18C7.75 18.1381 7.63807 18.25 7.5 18.25C7.36193 18.25 7.25 18.1381 7.25 18C7.25 17.8619 7.36193 17.75 7.5 17.75C7.63807 17.75 7.75 17.8619 7.75 18ZM16.75 14C16.75 14.1381 16.6381 14.25 16.5 14.25C16.3619 14.25 16.25 14.1381 16.25 14C16.25 13.8619 16.3619 13.75 16.5 13.75C16.6381 13.75 16.75 13.8619 16.75 14Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Notification02" (stroke rounded). */
    val Notification: ImageVector by lazy {
        hugeIcon(
            name = "Notification",
            paths = listOf(
                HugePath("M19 18V9.5C19 5.63401 15.866 2.5 12 2.5C8.13401 2.5 5 5.63401 5 9.5V18", isFill = false, strokeWidth = 1.5f),
                HugePath("M20.5 18H3.5", isFill = false, strokeWidth = 1.5f),
                HugePath("M13.5 20C13.5 20.8284 12.8284 21.5 12 21.5M10.5 20C10.5 20.8284 11.1716 21.5 12 21.5M12 21.5V20", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Sun03" (stroke rounded). */
    val Sun: ImageVector by lazy {
        hugeIcon(
            name = "Sun",
            paths = listOf(
                HugePath("M17 12C17 14.7614 14.7614 17 12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M12 2V3.5M12 20.5V22M19.0708 19.0713L18.0101 18.0106M5.98926 5.98926L4.9286 4.9286M22 12H20.5M3.5 12H2M19.0713 4.92871L18.0106 5.98937M5.98975 18.0107L4.92909 19.0714", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Medicine02" (stroke rounded). */
    val Medicine: ImageVector by lazy {
        hugeIcon(
            name = "Medicine",
            paths = listOf(
                HugePath("M20.1932 12.999C21.8501 15.8688 20.8669 19.5383 17.9971 21.1952C15.1273 22.8521 11.4578 21.8688 9.80094 18.999M20.1932 12.999C18.5364 10.1293 14.8669 9.14604 11.9971 10.8029C9.12734 12.4598 8.14409 16.1293 9.80094 18.999M20.1932 12.999L9.80094 18.999", isFill = false, strokeWidth = 1.5f),
                HugePath("M10.0428 5.54203L15.1278 2.5374C17 1.43112 19.394 2.08763 20.4749 4.00376C21.3433 5.54315 21.1 7.4272 20 8.6822M10.0428 5.54203L4.95785 8.54667C3.08563 9.65294 2.44415 12.1031 3.52508 14.0192C4.17499 15.1713 5.29956 15.868 6.5 16M10.0428 5.54203L11.5 8", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Walking" (stroke rounded). */
    val Walking: ImageVector by lazy {
        hugeIcon(
            name = "Walking",
            paths = listOf(
                HugePath("M6 12.5L7.73811 9.89287C7.91034 9.63452 8.14035 9.41983 8.40993 9.26578L10.599 8.01487C11.1619 7.69323 11.8483 7.67417 12.4282 7.9641C13.0851 8.29255 13.4658 8.98636 13.7461 9.66522C14.2069 10.7814 15.3984 12 18 12", isFill = false, strokeWidth = 1.5f),
                HugePath("M13 9L11.7772 14.5951M10.5 8.5L9.77457 11.7645C9.6069 12.519 9.88897 13.3025 10.4991 13.777L14 16.5L15.5 21", isFill = false, strokeWidth = 1.5f),
                HugePath("M9.5 16L9 17.5L6.5 20.5", isFill = false, strokeWidth = 1.5f),
                HugePath("M15 4.5C15 5.32843 14.3284 6 13.5 6C12.6716 6 12 5.32843 12 4.5C12 3.67157 12.6716 3 13.5 3C14.3284 3 15 3.67157 15 4.5Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Stethoscope" (stroke rounded). */
    val Stethoscope: ImageVector by lazy {
        hugeIcon(
            name = "Stethoscope",
            paths = listOf(
                HugePath("M13.0014 2C14.1053 2 15.0003 2.93126 15.0003 4.08003C15.0003 5.02915 15.0362 5.87375 14.2692 6.57196C11.7587 8.85732 10.5034 10 9.00027 10C7.49714 10 6.24187 8.85732 3.73133 6.57196C2.96426 5.87369 3.00027 5.029 3.00027 4.07981C3.00027 2.93116 3.8951 2 4.99893 2", isFill = false, strokeWidth = 1.5f),
                HugePath("M9 14V17.4998C9 19.9852 11.0149 22.0001 13.5003 22.0001C15.9858 22.0001 18.0007 19.9852 18.0007 17.4998V16", isFill = false, strokeWidth = 1.5f),
                HugePath("M14 7L12.6978 10.2556C12.3516 11.121 12.1785 11.5537 11.8887 11.9092C11.5988 12.2648 11.2098 12.5215 10.4319 13.0349L8.9696 14L7.53283 13.0323C6.77221 12.5201 6.39189 12.2639 6.10821 11.9126C5.82452 11.5613 5.65423 11.1356 5.31365 10.2841L4 7", isFill = false, strokeWidth = 1.5f),
                HugePath("M21 13C21 14.6569 19.6569 16 18 16C16.3431 16 15 14.6569 15 13C15 11.3431 16.3431 10 18 10C19.6569 10 21 11.3431 21 13Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M18.125 13H18M18.25 13C18.25 13.1381 18.1381 13.25 18 13.25C17.8619 13.25 17.75 13.1381 17.75 13C17.75 12.8619 17.8619 12.75 18 12.75C18.1381 12.75 18.25 12.8619 18.25 13Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Restaurant02" (stroke rounded). */
    val Restaurant: ImageVector by lazy {
        hugeIcon(
            name = "Restaurant",
            paths = listOf(
                HugePath("M4 21.001L7.00071 18", isFill = false, strokeWidth = 1.5f),
                HugePath("M15 10.001L14 11.001", isFill = false, strokeWidth = 1.5f),
                HugePath("M17.9999 3.00098L14.9999 6.00098C14.4547 6.54623 14.1821 6.81885 14.0363 7.11295C13.759 7.6725 13.759 8.32945 14.0363 8.88901C14.1821 9.1831 14.4547 9.45573 14.9999 10.001C15.5452 10.5462 15.8178 10.8189 16.1119 10.9646C16.6715 11.2419 17.3284 11.2419 17.888 10.9646C18.1821 10.8189 18.4547 10.5462 18.9999 10.001L21.9999 7.00098", isFill = false, strokeWidth = 1.5f),
                HugePath("M20 5L17 8", isFill = false, strokeWidth = 1.5f),
                HugePath("M20 21L12 13M12 13L2 3C2 6.84174 3.52612 10.5261 6.24264 13.2426L9 16L12 13Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Dumbbell01" (stroke rounded). */
    val Dumbbell: ImageVector by lazy {
        hugeIcon(
            name = "Dumbbell",
            paths = listOf(
                HugePath("M7.73438 13.7323C7.73438 13.7323 8.63984 12.5102 9.23438 12.2319C11.0292 11.3915 11.3943 11.0263 12.2344 9.23091C12.5127 8.63618 13.7344 7.73044 13.7344 7.73044M10.2344 16.2331C10.2344 16.2331 11.4561 15.3273 11.7344 14.7326C12.5745 12.9373 12.9396 12.5721 14.7344 11.7317C15.3289 11.4533 16.2344 10.2312 16.2344 10.2312", isFill = false, strokeWidth = 1.5f),
                HugePath("M14.4311 2.89207C14.938 2.38343 15.7611 2.38212 16.2696 2.88916L21.0814 7.68726C21.5899 8.1943 21.5912 9.01767 21.0843 9.52632L19.5557 11.0603C19.0488 11.5689 18.2257 11.5702 17.7172 11.0632L12.9054 6.26507C12.397 5.75803 12.3957 4.93466 12.9025 4.42601L14.4311 2.89207Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M4.41377 12.9022C4.92065 12.3936 5.74376 12.3923 6.25225 12.8993L11.0641 17.6974C11.5725 18.2045 11.5738 19.0278 11.067 19.5365L9.53836 21.0704C9.03148 21.5791 8.20837 21.5804 7.69988 21.0733L2.88808 16.2752C2.37959 15.7682 2.37829 14.9448 2.88517 14.4362L4.41377 12.9022Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M17.9377 3.45254C19.8201 0.985467 23.14 3.8401 20.5431 6.02872M3.37811 17.9773C0.998473 19.9687 3.99782 23.1586 6.06742 20.4657", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Call02" (stroke rounded). */
    val Call: ImageVector by lazy {
        hugeIcon(
            name = "Call",
            paths = listOf(
                HugePath("M9.1585 5.71217L8.75584 4.80619C8.49256 4.21382 8.36092 3.91762 8.16405 3.69095C7.91732 3.40688 7.59571 3.19788 7.23592 3.08779C6.94883 2.99994 6.6247 2.99994 5.97645 2.99994C5.02815 2.99994 4.554 2.99994 4.15597 3.18223C3.68711 3.39696 3.26368 3.86322 3.09497 4.35054C2.95175 4.76423 2.99278 5.18937 3.07482 6.03964C3.94815 15.0901 8.91006 20.052 17.9605 20.9254C18.8108 21.0074 19.236 21.0484 19.6496 20.9052C20.137 20.7365 20.6032 20.3131 20.818 19.8442C21.0002 19.4462 21.0002 18.972 21.0002 18.0237C21.0002 17.3755 21.0002 17.0514 20.9124 16.7643C20.8023 16.4045 20.5933 16.0829 20.3092 15.8361C20.0826 15.6393 19.7864 15.5076 19.194 15.2443L18.288 14.8417C17.6465 14.5566 17.3257 14.414 16.9998 14.383C16.6878 14.3533 16.3733 14.3971 16.0813 14.5108C15.7762 14.6296 15.5066 14.8543 14.9672 15.3038C14.4304 15.7511 14.162 15.9748 13.834 16.0946C13.5432 16.2009 13.1588 16.2402 12.8526 16.1951C12.5071 16.1442 12.2426 16.0028 11.7135 15.7201C10.0675 14.8404 9.15977 13.9327 8.28011 12.2867C7.99738 11.7576 7.85602 11.4931 7.80511 11.1476C7.75998 10.8414 7.79932 10.457 7.90554 10.1662C8.02536 9.83822 8.24905 9.5698 8.69643 9.03294C9.14586 8.49362 9.37058 8.22396 9.48939 7.91885C9.60309 7.62688 9.64686 7.31234 9.61719 7.00042C9.58618 6.67446 9.44362 6.3537 9.1585 5.71217Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Puzzle" (stroke rounded). */
    val Puzzle: ImageVector by lazy {
        hugeIcon(
            name = "Puzzle",
            paths = listOf(
                HugePath("M12.828 6.00096C12.9388 5.68791 12.999 5.35099 12.999 5C12.999 3.34315 11.6559 2 9.99904 2C8.34219 2 6.99904 3.34315 6.99904 5C6.99904 5.35099 7.05932 5.68791 7.17008 6.00096C4.88532 6.0093 3.66601 6.09039 2.87772 6.87868C2.08951 7.66689 2.00836 8.88603 2 11.1704C2.31251 11.06 2.64876 11 2.99904 11C4.6559 11 5.99904 12.3431 5.99904 14C5.99904 15.6569 4.6559 17 2.99904 17C2.64876 17 2.31251 16.94 2 16.8296C2.00836 19.114 2.08951 20.3331 2.87772 21.1213C3.66593 21.9095 4.88508 21.9907 7.16941 21.999C7.05908 21.6865 6.99904 21.3503 6.99904 21C6.99904 19.3431 8.34219 18 9.99904 18C11.6559 18 12.999 19.3431 12.999 21C12.999 21.3503 12.939 21.6865 12.8287 21.999C15.113 21.9907 16.3322 21.9095 17.1204 21.1213C17.9086 20.333 17.9897 19.1137 17.9981 16.829C18.3111 16.9397 18.648 17 18.999 17C20.6559 17 21.999 15.6569 21.999 14C21.999 12.3431 20.6559 11 18.999 11C18.648 11 18.3111 11.0603 17.9981 11.171C17.9897 8.88627 17.9086 7.66697 17.1204 6.87868C16.3321 6.09039 15.1128 6.0093 12.828 6.00096Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "Moon02" (stroke rounded). */
    val Moon: ImageVector by lazy {
        hugeIcon(
            name = "Moon",
            paths = listOf(
                HugePath("M21.5 14.0784C20.3003 14.7189 18.9402 15.0821 17.5 15.0821C12.7349 15.0821 8.87197 11.2191 8.87197 6.45404C8.87197 5.0142 9.22526 3.65685 9.85 2.45404C5.90122 3.45404 3 7.0142 3 11.4541C3 16.7198 7.26538 20.9852 12.5311 20.9852C16.9466 20.9852 20.6608 18.0824 21.5 14.0784Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "User" (stroke rounded). */
    val User: ImageVector by lazy {
        hugeIcon(
            name = "User",
            paths = listOf(
                HugePath("M16.5 6.5C16.5 8.98528 14.4853 11 12 11C9.51472 11 7.5 8.98528 7.5 6.5C7.5 4.01472 9.51472 2 12 2C14.4853 2 16.5 4.01472 16.5 6.5Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M5 19.5C5 16.4624 8.13401 14 12 14C15.866 14 19 16.4624 19 19.5C19 20.3284 18.3284 21 17.5 21H6.5C5.67157 21 5 20.3284 5 19.5Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "ArrowUpRight01" (stroke rounded). */
    val ArrowUpRight: ImageVector by lazy {
        hugeIcon(
            name = "ArrowUpRight",
            paths = listOf(
                HugePath("M7 17L17 7", isFill = false, strokeWidth = 2f),
                HugePath("M9 7H17V15", isFill = false, strokeWidth = 2f)
            )
        )
    }

    /** Hugeicons "FavouriteHeart" (stroke rounded). */
    val Heart: ImageVector by lazy {
        hugeIcon(
            name = "Heart",
            paths = listOf(
                HugePath("M19.4626 3.99426C16.7809 2.34528 14.4404 3.01182 13.0344 4.06628C12.4578 4.49849 12.1696 4.71459 12 4.71459C11.8304 4.71459 11.5422 4.49849 10.9656 4.06628C9.55962 3.01182 7.21909 2.34528 4.53744 3.99426C1.01807 6.15871 0.221721 13.2696 8.33953 19.6488C9.88572 20.8639 10.6588 21.4715 12 21.4715C13.3412 21.4715 14.1143 20.8639 15.6605 19.6488C23.7783 13.2696 22.9819 6.15871 19.4626 3.99426Z", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Hugeicons "CalendarCheck" (stroke rounded). */
    val CalendarCheck: ImageVector by lazy {
        hugeIcon(
            name = "CalendarCheck",
            paths = listOf(
                HugePath("M16 2V6M8 2V6", isFill = false, strokeWidth = 1.5f),
                HugePath("M3 10H21", isFill = false, strokeWidth = 1.5f),
                HugePath("M13 4H11C7.22876 4 5.34315 4 4.17157 5.17157C3 6.34315 3 8.22876 3 12V14C3 17.7712 3 19.6569 4.17157 20.8284C5.34315 22 7.22876 22 11 22H13C16.7712 22 18.6569 22 19.8284 20.8284C21 19.6569 21 17.7712 21 14V12C21 8.22876 21 6.34315 19.8284 5.17157C18.6569 4 16.7712 4 13 4Z", isFill = false, strokeWidth = 1.5f),
                HugePath("M8.5 16.8333C8.5 16.8333 9.375 16.8333 10.25 18.5C10.25 18.5 13.0294 14.3333 15.5 13.5", isFill = false, strokeWidth = 1.5f)
            )
        )
    }

    /** Rayo/energía (relleno) para la tarjeta de batería. */
    val Flash: ImageVector by lazy {
        hugeIcon(
            name = "Flash",
            paths = listOf(
                HugePath("M13 2L4.5 13.5H11L9.5 22L19.5 10.5H13L13 2Z", isFill = true, strokeWidth = 1.5f)
            )
        )
    }
}

private class HugePath(val d: String, val isFill: Boolean, val strokeWidth: Float)

private fun hugeIcon(name: String, paths: List<HugePath>): ImageVector {
    val builder = ImageVector.Builder(
        name = "Huge" + name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    paths.forEach { path ->
        builder.addPath(
            pathData = addPathNodes(path.d),
            fill = if (path.isFill) SolidColor(Color.Black) else null,
            stroke = if (path.isFill) null else SolidColor(Color.Black),
            strokeLineWidth = path.strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return builder.build()
}
