export interface MenuItem {
    label: string;
    icon: string;
    route: string;
    roles: string[];
    children?: MenuItem[];
    isOpen?: boolean;
    action?: () => void;
}
  